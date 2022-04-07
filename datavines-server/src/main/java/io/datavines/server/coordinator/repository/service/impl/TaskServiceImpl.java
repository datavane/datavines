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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.JSONUtils;
import io.datavines.server.coordinator.api.dto.task.SubmitTask;
import io.datavines.server.enums.CommandType;
import io.datavines.server.enums.Priority;
import io.datavines.server.coordinator.repository.entity.Command;
import io.datavines.server.coordinator.repository.mapper.CommandMapper;
import io.datavines.server.coordinator.repository.mapper.TaskMapper;
import io.datavines.server.coordinator.repository.service.TaskService;
import io.datavines.server.coordinator.repository.entity.Task;

@Service("taskService")
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>  implements TaskService {

    @Autowired
    private CommandMapper commandMapper;

    @Override
    public long insert(Task task) {
        baseMapper.insert(task);
        return task.getId();
    }

    @Override
    public int update(Task task) {
        return baseMapper.updateById(task);
    }

    @Override
    public Task getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<Task> listByDataSourceId(long dataSourceId) {
        return baseMapper.listByDataSourceId(dataSourceId);
    }

    @Override
    public Long submitTask(SubmitTask submitTask) {
        Task task = new Task();
        BeanUtils.copyProperties(submitTask,task);
        task.setParameter(JSONUtils.toJsonString(submitTask.getParameter()));
        task.setExecutePlatformParameter(JSONUtils.toJsonString(submitTask.getExecutePlatformParameter()));

        if("spark".equals(task.getEngineType())) {
            Map<String,Object> defaultEngineParameter = new HashMap<>();
            defaultEngineParameter.put("programType", "JAVA");
            defaultEngineParameter.put("deployMode", "cluster");
            defaultEngineParameter.put("driverCores", 1);
            defaultEngineParameter.put("driverMemory", "512M");
            defaultEngineParameter.put("numExecutors", 2);
            defaultEngineParameter.put("executorMemory", "2G");
            defaultEngineParameter.put("executorCores", 2);
            defaultEngineParameter.put("others", "--conf spark.yarn.maxAppAttempts=1");

            if (submitTask.getEngineParameter() != null) {
                defaultEngineParameter.putAll(submitTask.getEngineParameter());
            }
            submitTask.setEngineParameter(defaultEngineParameter);
            task.setEngineParameter(JSONUtils.toJsonString(submitTask.getEngineParameter()));
        }

        task.setSubmitTime(LocalDateTime.now());
        task.setStatus(ExecutionStatus.SUBMITTED_SUCCESS);
        Long taskId = insert(task);

        Command command = new Command();
        command.setType(CommandType.START);
        command.setPriority(Priority.MEDIUM);
        command.setTaskId(taskId);
        commandMapper.insert(command);

        return taskId;
    }

    @Override
    public Long killTask(Long taskId) {
        Command command = new Command();
        command.setType(CommandType.STOP);
        command.setPriority(Priority.MEDIUM);
        command.setTaskId(taskId);
        commandMapper.insert(command);

        return taskId;
    }

    @Override
    public List<Task> listNeedFailover(String host) {
        return baseMapper.selectList(new QueryWrapper<Task>()
                .eq("execute_host", host)
                .eq("status",ExecutionStatus.RUNNING_EXECUTION.getCode()));
    }

    @Override
    public List<Task> listTaskNotInServerList(List<String> hostList) {
        return baseMapper.selectList(new QueryWrapper<Task>()
                .in("execute_host", hostList)
                .eq("status",ExecutionStatus.RUNNING_EXECUTION.getCode()));
    }
}
