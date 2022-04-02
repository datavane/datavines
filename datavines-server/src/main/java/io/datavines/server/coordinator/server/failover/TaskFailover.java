package io.datavines.server.coordinator.server.failover;

import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.common.utils.NetUtils;
import io.datavines.common.utils.Stopper;
import io.datavines.common.utils.YarnUtils;
import io.datavines.server.command.TaskExecuteResponseCommand;
import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.server.coordinator.repository.service.impl.JobExternalService;
import io.datavines.server.coordinator.server.cache.TaskExecuteManager;
import io.datavines.server.utils.SpringApplicationContext;
import io.datavines.common.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskFailover {

    private static final Logger logger = LoggerFactory.getLogger(TaskFailover.class);

    private final JobExternalService jobExternalService;

    private final ConcurrentHashMap<Long,Task> needCheckStatusTaskMap = new ConcurrentHashMap<>();

    private final TaskExecuteManager taskExecuteManager;

    private final Integer SERVER_PORT =
            CommonPropertyUtils.getInt(CommonPropertyUtils.SERVER_PORT, CommonPropertyUtils.SERVER_PORT_DEFAULT);

    public TaskFailover(TaskExecuteManager taskExecuteManager){
        this.jobExternalService = SpringApplicationContext.getBean(JobExternalService.class);
        this.taskExecuteManager = taskExecuteManager;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(new YarnTaskStatusChecker(),0,4, TimeUnit.SECONDS);
    }

    public void handleTaskFailover(String host) {
        List<Task> taskList = jobExternalService.getTaskListNeedFailover(host);
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }

        innerHandleTaskFailover(taskList);
    }

    public void handleTaskFailover(List<String> hostList) {
        List<Task> taskList = jobExternalService.getTaskListNeedFailover(hostList);
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }

        innerHandleTaskFailover(taskList);
    }

    private void innerHandleTaskFailover(List<Task> taskList) {
        List<Task> needRerunTaskList = new ArrayList<>();

        taskList.forEach(task -> {
            if(StringUtils.isNotEmpty(task.getApplicationId())){
                try {
                    taskExecuteManager.addFailoverTaskRequest(task);
                } catch (DataVinesException e) {
                    e.printStackTrace();
                }
                task.setExecuteHost(NetUtils.getAddr(SERVER_PORT));
                jobExternalService.updateTask(task);
                needCheckStatusTaskMap.put(task.getId(), task);
            } else {
                String appId = YarnUtils.getYarnAppId(task.getTenantCode(), task.getApplicationIdTag());
                if (StringUtils.isNotEmpty(appId)) {
                    try {
                        taskExecuteManager.addFailoverTaskRequest(task);
                    } catch (DataVinesException e) {
                        e.printStackTrace();
                    }
                    task.setApplicationId(appId);
                    task.setExecuteHost(NetUtils.getAddr(SERVER_PORT));
                    jobExternalService.updateTask(task);
                    needCheckStatusTaskMap.put(task.getId(), task);
                } else {
                    needRerunTaskList.add(task);
                }
            }
        });

        handleRerunTask(needRerunTaskList);
    }

    private void handleRerunTask(List<Task> needRerunTaskList) {
        needRerunTaskList.forEach(task->{
            try {
                taskExecuteManager.addFailoverTaskRequest(task);
                TaskExecuteResponseCommand responseCommand =
                        new TaskExecuteResponseCommand(task.getId());
                responseCommand.setEndTime(LocalDateTime.now());
                responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
                taskExecuteManager.processTaskExecuteResponse(responseCommand);
            } catch (DataVinesException e) {
                e.printStackTrace();
            }
        });
    }

    class YarnTaskStatusChecker implements Runnable {

        @Override
        public void run() {

            if (Stopper.isRunning() && needCheckStatusTaskMap.size() > 0) {
                needCheckStatusTaskMap.forEach((k,v) ->{
                    TaskExecuteResponseCommand responseCommand =
                            new TaskExecuteResponseCommand(v.getId());
                    responseCommand.setEndTime(LocalDateTime.now());
                    ExecutionStatus applicationStatus = YarnUtils.getApplicationStatus(v.getApplicationId());
                    if (applicationStatus != null) {
                        logger.info("appId:{}, final state:{}", v.getApplicationId(), applicationStatus.name());
                        if (applicationStatus.equals(ExecutionStatus.FAILURE) ||
                                applicationStatus.equals(ExecutionStatus.KILL) ||
                                applicationStatus.equals(ExecutionStatus.SUCCESS)) {
                            responseCommand.setStatus(applicationStatus.getCode());
                            responseCommand.setApplicationIds(v.getApplicationId());
                            taskExecuteManager.processTaskExecuteResponse(responseCommand);
                            needCheckStatusTaskMap.remove(k);
                        }
                    }
                });
            }
        }
    }
}
