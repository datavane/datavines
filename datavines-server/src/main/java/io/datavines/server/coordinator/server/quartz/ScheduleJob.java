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

        Long jobId = dataMap.getLong(DataVinesConstants.DATASOURCE_JOB_ID);

        LocalDateTime scheduleTime = DateUtils.date2LocalDateTime(context.getScheduledFireTime());
        LocalDateTime fireTime = DateUtils.date2LocalDateTime(context.getFireTime());

        logger.info("scheduled fire time :{}, fire time :{}, job id :{}", scheduleTime, fireTime, jobId);

        Job job = getJobExternalService().getJobById(jobId);
        if (job == null) {
            logger.warn("job {} is null", jobId);
            return;
        }

        getJobExternalService().getJobService().execute(jobId, scheduleTime);
    }

}
