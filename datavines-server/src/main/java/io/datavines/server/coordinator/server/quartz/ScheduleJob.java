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
package io.datavines.server.coordinator.server.quartz;

import java.time.LocalDateTime;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.server.DataVinesConstants;
import io.datavines.server.enums.CommandType;
import io.datavines.server.enums.Priority;
import io.datavines.server.coordinator.repository.service.impl.JobExternalService;
import io.datavines.server.utils.SpringApplicationContext;
import io.datavines.common.utils.DateUtils;
import io.datavines.server.coordinator.repository.entity.Command;
import io.datavines.server.coordinator.repository.entity.Job;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.common.enums.ExecutionStatus;

/**
 * process schedule job
 */
public class ScheduleJob implements org.quartz.Job {

    /**
     * logger of FlowScheduleJob
     */
    private static final Logger logger = LoggerFactory.getLogger(ScheduleJob.class);

    public JobExternalService getJobExternalService(){
        return SpringApplicationContext.getBean(JobExternalService.class);
    }

    /**
     * Called by the Scheduler when a Trigger fires that is associated with the Job
     *
     * @param context JobExecutionContext
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        Long projectJobId = dataMap.getLong(DataVinesConstants.PROJECT_JOB_ID);
        Long projectId = dataMap.getLong(DataVinesConstants.PROJECT_ID);

        LocalDateTime scheduleTime = DateUtils.date2LocalDateTime(context.getScheduledFireTime());
        LocalDateTime fireTime = DateUtils.date2LocalDateTime(context.getFireTime());

        logger.info("scheduled fire time :{}, fire time :{}, process id :{}", scheduleTime, fireTime, projectJobId);

        Job job = getJobExternalService().getJobById(projectJobId);
        if (job == null) {
            logger.warn("job {} is null", projectJobId);
            return;
        }

        Task task = createTask(job,scheduleTime);

        createCommand(task,scheduleTime,fireTime);
    }

    private void deleteJob(Long projectId, long scheduleId) throws RuntimeException{
        logger.info("delete schedules of project id:{}, schedule id:{}", projectId, scheduleId);

        String jobName = QuartzExecutors.buildJobName(scheduleId);
        String jobGroupName = QuartzExecutors.buildJobGroupName(projectId);

//        if(!QuartzExecutors.getInstance().deleteJob(jobName, jobGroupName)){
//            logger.warn("set offline failure:projectId:{},scheduleId:{}",projectId,scheduleId);
//            throw new RuntimeException("set offline failure");
//        }
    }

    private Task createTask(Job job, LocalDateTime scheduleTime){
        Task task = new Task();
        task.setName(job.getName());
        task.setJobId(job.getId());
        task.setDataSourceId(job.getDataSourceId());
        task.setParameter(job.getParameter());
        task.setStatus(ExecutionStatus.SUBMITTED_SUCCESS);
        task.setRetryTimes(job.getRetryTimes());
        task.setRetryInterval(job.getRetryInterval());
        task.setTimeout(job.getTimeout());
        task.setTimeoutStrategy(job.getTimeoutStrategy());
//        task.setTenantCode(job.getTenantCode());
        task.setSubmitTime(scheduleTime);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        return task;
    }

    private void createCommand(Task task, LocalDateTime scheduleTime, LocalDateTime fireTime) {
        Command command = new Command();
        command.setTaskId(task.getId());
        command.setType(CommandType.SCHEDULER);
        command.setPriority(Priority.MEDIUM);
        command.setCreateTime(LocalDateTime.now());
        command.setUpdateTime(LocalDateTime.now());
        getJobExternalService().insertCommand(command);
    }
}
