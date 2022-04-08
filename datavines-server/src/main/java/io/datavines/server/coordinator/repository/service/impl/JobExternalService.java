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

package io.datavines.server.coordinator.repository.service.impl;

import io.datavines.common.config.DataVinesQualityConfig;
import io.datavines.common.entity.TaskInfo;
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.JSONUtils;
import io.datavines.engine.config.DataVinesConfigurationManager;
import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.repository.entity.Command;
import io.datavines.server.coordinator.repository.entity.Job;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.server.coordinator.repository.entity.TaskResult;
import io.datavines.server.coordinator.repository.service.*;
import io.datavines.server.utils.DefaultDataSourceInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JobExternalService {
    
    @Autowired
    private TaskService taskService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private JobService jobService;

    @Autowired
    private TaskResultService taskResultService;

    @Autowired
    private ActualValuesService actualValuesService;

    public Job getJobById(Long id) {
        return jobService.getById(id);
    }

    public Task getTaskById(Long id){
        return taskService.getById(id);
    }

    public Command getCommand(){
        return commandService.getOne();
    }

    public int deleteCommandById(long id){
        return commandService.deleteById(id);
    }

    public Task executeCommand(Command command){
        Task task = taskService.getById(command.getTaskId());
        task.setStartTime(LocalDateTime.now());
        taskService.update(task);
        return task;
    }

    public int updateTask(Task task){
        return taskService.update(task);
    }

    public Long insertTask(Task task){
        return taskService.insert(task);
    }

    public Long insertCommand(Command command){
        return commandService.insert(command);
    }

    public int updateCommand(Command command){
        return commandService.update(command);
    }

    public void updateTaskStatus(Long taskId, ExecutionStatus status){
        Task task = getTaskById(taskId);
        task.setStatus(status);
        updateTask(task);
    }

    public void updateTaskRetryTimes(Long taskId, int times) {
        Task task = getTaskById(taskId);
        task.setRetryTimes(times);
        updateTask(task);
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
        //读取配置文件获取环境信息
        taskRequest.setExecutePlatformParameter(task.getExecutePlatformParameter());
        taskRequest.setEngineType(task.getEngineType());
        taskRequest.setEngineParameter(task.getEngineParameter());
        Map<String,String> inputParameter = new HashMap<>();

        TaskInfo taskInfo = new TaskInfo(task.getId(),
                                         task.getName(),task.getEngineType(),
                                         task.getEngineParameter(),taskParameter);
        DataVinesQualityConfig qualityConfig =
                DataVinesConfigurationManager.generateConfiguration(inputParameter, taskInfo, DefaultDataSourceInfoUtils.getDefaultConnectionInfo());
        taskRequest.setApplicationParameter(JSONUtils.toJsonString(qualityConfig));
        taskRequest.setTenantCode(task.getTenantCode());
        taskRequest.setRetryTimes(task.getRetryTimes());
        taskRequest.setRetryInterval(task.getRetryInterval());
        taskRequest.setTimeout(task.getTimeout());
        taskRequest.setTimeoutStrategy(task.getTimeoutStrategy());
        taskRequest.setEnv(task.getEnv());
        return taskRequest;
    }

    public TaskResult getTaskResultByTaskId(long taskId) {
        return taskResultService.getByTaskId(taskId);
    }

    public int deleteTaskResultByTaskId(long taskId) {
        return taskResultService.deleteByTaskId(taskId);
    }

    public int deleteActualValuesByTaskId(long taskId) {
        return actualValuesService.deleteByTaskId(taskId);
    }

    public int updateTaskResult(TaskResult taskResult) {
        return taskResultService.update(taskResult);
    }

    public List<Task> getTaskListNeedFailover(String host){
        return taskService.listNeedFailover(host);
    }

    public List<Task> getTaskListNeedFailover(List<String> host){
        return taskService.listTaskNotInServerList(host);
    }
}
