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
package io.datavines.server.executor.runner;

import java.time.LocalDateTime;

import io.datavines.common.config.Configurations;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.LoggerUtils;
import io.datavines.engine.api.engine.EngineExecutor;
import io.datavines.server.coordinator.server.cache.TaskExecuteManager;
import io.datavines.server.command.TaskExecuteResponseCommand;
import io.datavines.spi.PluginLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRunner implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TaskRunner.class);

    private final TaskRequest taskRequest;

    private final TaskExecuteManager taskExecuteManager;

    private EngineExecutor engineExecutor;

    private final Configurations configurations;

    public TaskRunner(TaskRequest taskRequest, TaskExecuteManager taskExecuteManager, Configurations configurations){
        this.taskRequest = taskRequest;
        this.taskExecuteManager = taskExecuteManager;
        this.configurations = configurations;
    }

    @Override
    public void run() {
        TaskExecuteResponseCommand responseCommand =
                new TaskExecuteResponseCommand(this.taskRequest.getTaskId());
        try {
            String taskLoggerName = LoggerUtils.buildTaskLoggerName(
                    LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                    taskRequest.getTaskUniqueId());

            // custom logger
            Logger taskLogger = LoggerFactory.getLogger(taskLoggerName);
            Thread.currentThread().setName(taskLoggerName);

            engineExecutor = PluginLoader
                    .getPluginLoader(EngineExecutor.class)
                    .getNewPlugin(taskRequest.getEngineType());

            engineExecutor.init(taskRequest, taskLogger, configurations);
            engineExecutor.execute();
            engineExecutor.after();

            if (engineExecutor.isCancel()) {
                responseCommand.setStatus(ExecutionStatus.KILL.getCode());
            } else {
                responseCommand.setStatus(engineExecutor.getProcessResult().getExitStatusCode());
            }

            responseCommand.setEndTime(LocalDateTime.now());
            responseCommand.setApplicationIds(engineExecutor.getProcessResult().getApplicationId());
            responseCommand.setProcessId(engineExecutor.getProcessResult().getProcessId());

        } catch (Exception e) {
            logger.error("task execute failure", e);
            kill();
            try {
                if (engineExecutor.isCancel()) {
                    responseCommand.setStatus(ExecutionStatus.KILL.getCode());
                } else {
                    responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
                }
            } catch (Exception ex) {
                logger.error("task execute failure", ex);
            }

            responseCommand.setEndTime(LocalDateTime.now());
            responseCommand.setApplicationIds(engineExecutor.getProcessResult().getApplicationId());
            responseCommand.setProcessId(engineExecutor.getProcessResult().getProcessId());
        } finally {
            taskExecuteManager.processTaskExecuteResponse(responseCommand);
        }
    }

    /**
     *  kill job
     */
    public void kill(){
        if (engineExecutor != null) {
            try {
                engineExecutor.cancel();
                TaskExecuteResponseCommand responseCommand =
                        new TaskExecuteResponseCommand(this.taskRequest.getTaskId());
                responseCommand.setStatus(ExecutionStatus.KILL.getCode());
                responseCommand.setEndTime(LocalDateTime.now());
                responseCommand.setApplicationIds(engineExecutor.getProcessResult().getApplicationId());
                responseCommand.setProcessId(engineExecutor.getProcessResult().getProcessId());
                taskExecuteManager.processTaskExecuteResponse(responseCommand);

            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
    }
}
