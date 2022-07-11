/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.server.coordinator.server.cache;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.datavines.common.CommonConstants;
import io.datavines.common.config.Configurations;
import io.datavines.common.config.DataVinesQualityConfig;
import io.datavines.common.entity.TaskInfo;
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.log.TaskLogDiscriminator;
import io.datavines.common.utils.*;
import io.datavines.engine.config.DataVinesConfigurationManager;
import io.datavines.server.command.CommandCode;
import io.datavines.server.command.TaskExecuteAckCommand;
import io.datavines.server.command.TaskExecuteResponseCommand;
import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.server.coordinator.repository.service.impl.JobExternalService;
import io.datavines.server.coordinator.server.operator.DataQualityResultOperator;
import io.datavines.server.executor.cache.TaskExecutionCache;
import io.datavines.server.executor.cache.TaskExecutionContext;
import io.datavines.server.executor.runner.TaskRunner;
import io.datavines.server.utils.DefaultDataSourceInfoUtils;
import io.datavines.server.utils.NamedThreadFactory;
import io.datavines.server.utils.SpringApplicationContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class TaskExecuteManager {

    private final Logger logger = LoggerFactory.getLogger(TaskExecuteManager.class);

    private final LinkedBlockingQueue<CommandContext> taskQueue = new LinkedBlockingQueue<>();

    private final ConcurrentHashMap<Long, TaskRequest> unFinishedTaskMap = new ConcurrentHashMap<>();
    
    private final LinkedBlockingQueue<TaskResponseContext> responseQueue = new LinkedBlockingQueue<>();

    private final ExecutorService executorService;

    private final ExecutorService taskExecuteService;

    private final JobExternalService jobExternalService;

    private final TaskExecutionCache taskExecutionCache;

    private final HashedWheelTimer wheelTimer =
            new HashedWheelTimer(new NamedThreadFactory("Task-Execute-Timeout"),1,TimeUnit.SECONDS);

    private final Configurations configurations;

    private final DataQualityResultOperator dataQualityResultOperator;

    public TaskExecuteManager(){

        this.executorService = Executors.newFixedThreadPool(5, new NamedThreadFactory("Server-thread"));
        this.taskExecuteService = Executors.newFixedThreadPool(
                CommonPropertyUtils.getInt(CommonPropertyUtils.EXEC_THREADS,CommonPropertyUtils.EXEC_THREADS_DEFAULT),
                new NamedThreadFactory("Executor-execute-thread"));
        this.jobExternalService = SpringApplicationContext.getBean(JobExternalService.class);
        this.taskExecutionCache = TaskExecutionCache.getInstance();
        this.configurations = new Configurations(CommonPropertyUtils.getProperties());
        this.dataQualityResultOperator = SpringApplicationContext.getBean(DataQualityResultOperator.class);
    }

    public void start() {
        TaskExecutor taskExecutor = new TaskExecutor();
        executorService.submit(taskExecutor);
        logger.info("task sender start");

        TaskResponseOperator responseOperator = new TaskResponseOperator();
        executorService.submit(responseOperator);
        logger.info("task response operator start");
    }

    public void addExecuteCommand(TaskRequest taskRequest){
        logger.info("put into wait queue to send {}", JSONUtils.toJsonString(taskRequest));
        unFinishedTaskMap.put(taskRequest.getTaskId(), taskRequest);
        CommandContext commandContext = new CommandContext();
        commandContext.setCommandCode(CommandCode.TASK_EXECUTE_REQUEST);
        commandContext.setTaskId(taskRequest.getTaskId());
        commandContext.setTaskRequest(taskRequest);
        taskQueue.offer(commandContext);
    }

    public void addKillCommand(Long taskId){
        CommandContext commandContext = new CommandContext();
        commandContext.setCommandCode(CommandCode.TASK_KILL_REQUEST);
        commandContext.setTaskId(taskId);
        taskQueue.offer(commandContext);
    }

    public TaskRequest getExecutingTask(Long taskId){
        return unFinishedTaskMap.get(taskId);
    }

    class TaskExecutor implements Runnable {

        @Override
        public void run() {
            while(Stopper.isRunning()) {
                try {
                    CommandContext commandContext = taskQueue.take();
                    Long taskId = commandContext.getTaskId();
                    TaskRequest taskRequest = commandContext.getTaskRequest();
                    if (unFinishedTaskMap.get(taskId) == null) {
                        continue;
                    }
                    switch(commandContext.getCommandCode()){
                        case TASK_EXECUTE_REQUEST:
                            executeTask(taskRequest);
                            wheelTimer.newTimeout(
                                    new TaskTimeoutTimerTask(
                                            taskId,taskRequest.getRetryTimes()),
                                            taskRequest.getTimeout()+2, TimeUnit.SECONDS);
                            break;
                        case TASK_KILL_REQUEST:
                            doKillCommand(taskId);
                            break;
                        default:
                            break;
                    }

                } catch(Exception e) {
                    logger.error("dispatcher job error",e);
                    ThreadUtils.sleep(2000);
                }
            }
        }
    }

    private void executeTask(TaskRequest taskRequest) {
        String execLocalPath = getExecLocalPath(taskRequest);
        try {
            FileUtils.createWorkDirAndUserIfAbsent(execLocalPath, taskRequest.getTenantCode());
        } catch (Exception ex){
            logger.error(String.format("create execLocalPath : %s", execLocalPath), ex);
        }
        taskRequest.setExecuteFilePath(execLocalPath);
        Path path  = new File(execLocalPath).toPath();

        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch(IOException e) {
            logger.info("delete path error {0}",e);
        }

        taskRequest.setTaskUniqueId(
                buildTaskUniqueId(
                        taskRequest.getExecutePlatformType(),
                        taskRequest.getEngineType(),
                        taskRequest.getTaskId()));
        taskRequest.setStartTime(LocalDateTime.now());
        taskRequest.setTimeout(taskRequest.getTimeout()*2);
        taskRequest.setExecuteHost(NetUtils.getAddr(
                CommonPropertyUtils.getInt(CommonPropertyUtils.SERVER_PORT, CommonPropertyUtils.SERVER_PORT_DEFAULT)));
        doAck(taskRequest);

        TaskRunner taskRunner = new TaskRunner(taskRequest, this, configurations);
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskRequest(taskRequest);
        taskExecutionContext.setTaskRunner(taskRunner);
        taskExecutionCache.cache(taskExecutionContext);
        taskExecuteService.submit(taskRunner);
    }

    public String buildTaskUniqueId(String executePlatformType, String engineType, long taskId){
        // LOCAL_SPARK_1521012323213]
        return String.format("%s_%s_%s",
                executePlatformType.toLowerCase(),
                engineType.toLowerCase(),
                taskId);
    }

    private void doAck(TaskRequest taskRequest){
        processTaskAckResponse(buildAckCommand(taskRequest));
    }

    private TaskExecuteAckCommand buildAckCommand(TaskRequest taskRequest) {
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand(taskRequest.getTaskId());
        ackCommand.setStatus(ExecutionStatus.RUNNING_EXECUTION.getCode());
        ackCommand.setLogPath(getTaskLogPath(taskRequest));
        ackCommand.setHost(taskRequest.getExecuteHost());
        ackCommand.setStartTime(LocalDateTime.now());
        ackCommand.setExecutePath(taskRequest.getExecuteFilePath());
        taskRequest.setLogPath(ackCommand.getLogPath());
        return ackCommand;
    }

    /**
     * get job log path
     * @return log path
     */
    private String getTaskLogPath(TaskRequest taskRequest) {
        String baseLog = ((TaskLogDiscriminator) ((SiftingAppender) ((LoggerContext) LoggerFactory.getILoggerFactory())
                .getLogger("ROOT")
                .getAppender("TASK_LOG_FILE"))
                .getDiscriminator()).getLogBase();
        if (baseLog.startsWith(CommonConstants.SINGLE_SLASH)){
            return baseLog
                    + CommonConstants.SINGLE_SLASH + LoggerUtils.TASK_LOGGER_INFO_PREFIX.toLowerCase()
                    + CommonConstants.SINGLE_SLASH + DateUtils.format(LocalDateTime.now(),DateUtils.YYYYMMDD)
                    + CommonConstants.SINGLE_SLASH + taskRequest.getTaskUniqueId() + ".log";
        }

        return System.getProperty("user.dir")
                + CommonConstants.SINGLE_SLASH + baseLog
                + CommonConstants.SINGLE_SLASH + LoggerUtils.TASK_LOGGER_INFO_PREFIX.toLowerCase()
                + CommonConstants.SINGLE_SLASH + DateUtils.format(LocalDateTime.now(),DateUtils.YYYYMMDD)
                + CommonConstants.SINGLE_SLASH + taskRequest.getTaskUniqueId() + ".log";
    }

    /**
     * get execute local path
     * @param taskRequest executionJob
     * @return execute local path
     */
    private String getExecLocalPath(TaskRequest taskRequest){
        return FileUtils.getTaskExecDir(
                taskRequest.getEngineType(),
                taskRequest.getTaskId());
    }

    /**
     * operate the job response
     */
    class TaskResponseOperator implements Runnable {

        @Override
        public void run() {
            while (Stopper.isRunning()) {
                try {
                    TaskResponseContext taskResponse = responseQueue.take();
                    TaskRequest taskRequest = taskResponse.getTaskRequest();
                    Task task = jobExternalService.getTaskById(taskRequest.getTaskId());
                    switch (taskResponse.getCommandCode()) {
                        case TASK_EXECUTE_ACK:
                            if (task != null) {
                                task.setStartTime(taskRequest.getStartTime());
                                task.setStatus(ExecutionStatus.of(taskRequest.getStatus()));
                                task.setExecuteFilePath(taskRequest.getExecuteFilePath());
                                task.setLogPath(taskRequest.getLogPath());
                                task.setApplicationIdTag(taskRequest.getTaskUniqueId());
                                task.setExecuteHost(taskRequest.getExecuteHost());
                                jobExternalService.updateTask(task);
                            }
                            break;
                        case TASK_EXECUTE_RESPONSE:
                            logger.info("task execute response: " + JSONUtils.toJsonString(taskRequest));
                            unFinishedTaskMap.put(taskRequest.getTaskId(),taskRequest);
                            if (ExecutionStatus.of(taskRequest.getStatus()).typeIsSuccess()) {
                                unFinishedTaskMap.remove(taskRequest.getTaskId());
                                task.setApplicationId(taskRequest.getApplicationId());
                                task.setProcessId(taskRequest.getProcessId());
                                task.setExecuteHost(taskRequest.getExecuteHost());
                                task.setEndTime(taskRequest.getEndTime());
                                task.setStatus(ExecutionStatus.of(taskRequest.getStatus()));
                                jobExternalService.updateTask(task);
                                dataQualityResultOperator.operateDqExecuteResult(taskRequest);
                            } else if (ExecutionStatus.of(taskRequest.getStatus()).typeIsFailure()) {
                                int retryNums = task.getRetryTimes();
                                if (task.getRetryTimes() > 0) {
                                    logger.info("retry task: "+JSONUtils.toJsonString(task));
                                    CommandContext commandContext = new CommandContext();
                                    commandContext.setTaskRequest(jobExternalService.buildTaskRequest(task));
                                    commandContext.setTaskId(taskRequest.getTaskId());
                                    commandContext.setCommandCode(CommandCode.TASK_EXECUTE_REQUEST);
                                    taskQueue.offer(commandContext);
                                    jobExternalService.updateTaskRetryTimes(taskRequest.getTaskId(), retryNums - 1);
                                    jobExternalService.deleteTaskResultByTaskId(taskRequest.getTaskId());
                                    jobExternalService.deleteActualValuesByTaskId(taskRequest.getTaskId());
                                } else {
                                    updateTaskAndRemoveCache(taskRequest, task);
                                }
                            } else if(ExecutionStatus.of(taskRequest.getStatus()).typeIsCancel()) {
                                updateTaskAndRemoveCache(taskRequest, task);
                            } else if(ExecutionStatus.of(taskRequest.getStatus()).typeIsRunning()) {
                                // do nothing
                            }

                            break;
                        default:
                            break;
                    }
                } catch(Exception e) {
                    logger.info("operate job response error {0}",e);
                }
            }
        }
    }

    private void updateTaskAndRemoveCache(TaskRequest taskRequest, Task task) {
        unFinishedTaskMap.remove(taskRequest.getTaskId());
        jobExternalService.deleteTaskResultByTaskId(taskRequest.getTaskId());
        jobExternalService.deleteActualValuesByTaskId(taskRequest.getTaskId());
        task.setApplicationId(taskRequest.getApplicationId());
        task.setProcessId(taskRequest.getProcessId());
        task.setExecuteHost(taskRequest.getExecuteHost());
        task.setEndTime(taskRequest.getEndTime());
        task.setStatus(ExecutionStatus.of(taskRequest.getStatus()));
        jobExternalService.updateTask(task);
    }

    /**
     * put the response into queue
     * @param taskResponseContext taskResponseContext
     */
    public void putResponse(TaskResponseContext taskResponseContext){
        responseQueue.offer(taskResponseContext);
    }

    class TaskTimeoutTimerTask implements TimerTask {

        private final long taskId;
        private final int retryTimes;

        public TaskTimeoutTimerTask(long taskId, int retryTimes){
            this.taskId = taskId;
            this.retryTimes = retryTimes;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            TaskRequest taskRequest = unFinishedTaskMap.get(this.taskId);
            if (taskRequest == null) {
                logger.info("task {} is finished, do nothing...",taskId);
                return;
            }

            if (this.retryTimes != taskRequest.getRetryTimes()) {
                logger.info("task {} is finished, do nothing...",taskId);
                return;
            }

            logger.info("task {} is timeout, do something",taskId);

        }
    }

    private void doKillCommand(Long taskId) {
        TaskExecutionContext taskExecutionContext = taskExecutionCache.getById(taskId);

        if (taskExecutionContext != null){
            TaskRunner taskRunner = taskExecutionContext.getTaskRunner();
            taskRunner.kill();
        } else {
            unFinishedTaskMap.remove(taskId);
            Task task = jobExternalService.getTaskById(taskId);
            if (task != null) {
                task.setEndTime(LocalDateTime.now());
                task.setStatus(ExecutionStatus.KILL);
                jobExternalService.updateTask(task);
            }
        }
    }

    public void processTaskExecuteResponse(TaskExecuteResponseCommand taskExecuteResponseCommand) {

        TaskRequest taskRequest = getExecutingTask(taskExecuteResponseCommand.getTaskId());
        if(taskRequest == null){
            taskRequest =  new TaskRequest();
        }

        taskRequest.setTaskId(taskExecuteResponseCommand.getTaskId());
        taskRequest.setEndTime(taskExecuteResponseCommand.getEndTime());
        taskRequest.setStatus(taskExecuteResponseCommand.getStatus());
        taskRequest.setApplicationId(taskExecuteResponseCommand.getApplicationIds());
        taskRequest.setProcessId(taskExecuteResponseCommand.getProcessId());

        TaskResponseContext taskResponseContext = new TaskResponseContext(CommandCode.TASK_EXECUTE_RESPONSE, taskRequest);

        putResponse(taskResponseContext);
    }

    public void processTaskAckResponse(TaskExecuteAckCommand taskExecuteAckCommand) {
        TaskRequest taskRequest = getExecutingTask(taskExecuteAckCommand.getTaskId());
        if (taskRequest == null) {
            taskRequest =  new TaskRequest();
        }

        taskRequest.setTaskId(taskExecuteAckCommand.getTaskId());
        taskRequest.setStartTime(taskExecuteAckCommand.getStartTime());
        taskRequest.setStatus(taskExecuteAckCommand.getStatus());
        taskRequest.setExecuteHost(taskExecuteAckCommand.getHost());
        taskRequest.setLogPath(taskExecuteAckCommand.getLogPath());
        taskRequest.setExecuteFilePath(taskExecuteAckCommand.getExecutePath());

        TaskResponseContext taskResponseContext = new TaskResponseContext(CommandCode.TASK_EXECUTE_ACK, taskRequest);

        putResponse(taskResponseContext);
    }

    public void close(){
        if (executorService != null) {
            executorService.shutdown();
        }

        if (taskExecuteService != null) {
            taskExecuteService.shutdown();
        }
    }

    public void addFailoverTaskRequest(Task task) throws DataVinesException {
        TaskRequest taskRequest = buildTaskRequest(task);
        unFinishedTaskMap.put(taskRequest.getTaskId(), taskRequest);
    }

    public TaskRequest buildTaskRequest(Task task) throws DataVinesException {
        // need to convert job parameter to other parameter
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(task.getId());
        taskRequest.setTaskName(task.getName());
        TaskParameter taskParameter = JSONUtils.parseObject(task.getParameter(),TaskParameter.class);
        if (taskParameter == null) {
            throw new DataVinesException("TaskParameter can not be null");
        }

        taskRequest.setExecutePlatformType(task.getExecutePlatformType());
        taskRequest.setExecutePlatformParameter(task.getExecutePlatformParameter());
        taskRequest.setEngineType(task.getEngineType());
        taskRequest.setEngineParameter(task.getEngineParameter());
        taskRequest.setErrorDataStorageType(task.getErrorDataStorageType());
        taskRequest.setErrorDataStorageParameter(task.getErrorDataStorageParameter());
        Map<String,String> inputParameter = new HashMap<>();

        TaskInfo taskInfo = new TaskInfo(task.getId(), task.getName(),
                task.getEngineType(), task.getEngineParameter(),
                task.getErrorDataStorageType(), task.getErrorDataStorageParameter(),
                taskParameter);
        DataVinesQualityConfig qualityConfig =
                DataVinesConfigurationManager.generateConfiguration(inputParameter, taskInfo, DefaultDataSourceInfoUtils.getDefaultConnectionInfo());

        taskRequest.setExecuteFilePath(task.getExecuteFilePath());
        taskRequest.setLogPath(task.getLogPath());
        taskRequest.setTaskUniqueId(task.getApplicationIdTag());
        taskRequest.setStatus(task.getStatus().getCode());
        taskRequest.setExecuteHost(NetUtils.getAddr(CommonPropertyUtils.getInt(CommonPropertyUtils.SERVER_PORT, CommonPropertyUtils.SERVER_PORT_DEFAULT)));
        taskRequest.setApplicationParameter(JSONUtils.toJsonString(qualityConfig));
        taskRequest.setTenantCode(task.getTenantCode());
        taskRequest.setRetryTimes(task.getRetryTimes());
        taskRequest.setRetryInterval(task.getRetryInterval());
        taskRequest.setTimeout(task.getTimeout());
        taskRequest.setTimeoutStrategy(task.getTimeoutStrategy());
        taskRequest.setEnv(task.getEnv());
        taskRequest.setApplicationId(task.getApplicationId());
        taskRequest.setProcessId(task.getProcessId());
        return taskRequest;
    }
}
