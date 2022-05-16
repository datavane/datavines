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
import java.util.List;

import io.datavines.common.dto.job.JobCreate;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.JSONUtils;
import io.datavines.server.coordinator.repository.entity.Command;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.server.coordinator.repository.mapper.CommandMapper;
import io.datavines.server.coordinator.repository.mapper.TaskMapper;
import io.datavines.server.enums.CommandType;
import io.datavines.server.enums.JobType;
import io.datavines.server.enums.Priority;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.server.coordinator.repository.mapper.JobMapper;
import io.datavines.server.coordinator.repository.service.JobService;
import io.datavines.server.coordinator.repository.entity.Job;

@Service("jobService")
public class JobServiceImpl extends ServiceImpl<JobMapper,Job> implements JobService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Override
    public long insert(Job job) {
        baseMapper.insert(job);
        return job.getId();
    }

    @Override
    public int update(Job job) {
        return baseMapper.updateById(job);
    }

    @Override
    public Job getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<Job> listByDataSourceId(Long dataSourceId) {
        return baseMapper.listByDataSourceId(dataSourceId);
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public long createJob(JobCreate jobCreate) {

        Job job = new Job();
        BeanUtils.copyProperties(jobCreate, job);
        job.setParameter(JSONUtils.toJsonString(jobCreate.getParameter()));

        job.setType(JobType.valueOf(jobCreate.getType()));
        job.setRetryTimes(10000);
        job.setRetryInterval(60000);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());

        insert(job);

        // add a job
        long jobId = job.getId();


        // whether running now
        if(jobCreate.getRunningNow() == 1) {
            // add a task
            Task task = new Task();
            BeanUtils.copyProperties(jobCreate, task);
            task.setParameter(JSONUtils.toJsonString(jobCreate.getParameter()));

            task.setName(jobCreate.getName() + "_task_" + System.currentTimeMillis());
            task.setJobId(jobId);
            task.setJobType(job.getType());
            task.setStatus(ExecutionStatus.SUBMITTED_SUCCESS);
            task.setSubmitTime(LocalDateTime.now());
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            taskMapper.insert(task);



            System.out.println(task.getId());
            // add a command
            Command command = new Command();
            command.setType(CommandType.START);
            command.setPriority(Priority.MEDIUM);
            command.setTaskId(task.getId());
            commandMapper.insert(command);
        }
        return jobId;
    }
}
