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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleCreate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleUpdate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.MapParam;
import io.datavines.server.coordinator.repository.entity.Job;
import io.datavines.server.coordinator.repository.entity.JobSchedule;
import io.datavines.server.coordinator.repository.mapper.JobScheduleMapper;
import io.datavines.server.coordinator.repository.service.JobScheduleService;
import io.datavines.server.coordinator.server.quartz.QuartzExecutors;
import io.datavines.server.coordinator.server.quartz.ScheduleJob;
import io.datavines.server.coordinator.server.quartz.StrategyFactory;
import io.datavines.server.coordinator.server.quartz.cron.FunCron;
import io.datavines.server.exception.DataVinesServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Slf4j
@Service("JobScheduleService")
public class JobScheduleServiceImpl extends ServiceImpl<JobScheduleMapper, JobSchedule>  implements JobScheduleService {

    @Autowired
    private QuartzExecutors quartzExecutor;
    @Override
    public long create(JobScheduleCreate jobScheduleCreateCreate) throws DataVinesServerException {
        String cron = "";
        try {
            JobSchedule jobSchedule = new JobSchedule();
            BeanUtils.copyProperties(jobScheduleCreateCreate, jobSchedule);
            String type = jobScheduleCreateCreate.getType();
            MapParam param=jobScheduleCreateCreate.getParam();
            ObjectMapper mapper = new ObjectMapper();
            String result1 = mapper.writeValueAsString(param);
            jobSchedule.setParam(result1);
            log.info(result1);
            FunCron api =  StrategyFactory.getByNum(1);
            cron = api.funcDeal(jobSchedule);
            jobSchedule.setCron_expression(cron);

            //quartzExecutor.deleteJob(jobSchedule.getJobId(), 1);
            quartzExecutor.addJob(ScheduleJob.class, 1,  jobSchedule);

            System.out.println(cron);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int update(JobScheduleUpdate jobScheduleUpdate) throws DataVinesServerException {
        return 0;
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }



    @Override
    public JobSchedule getById(long id) {
        return baseMapper.selectById(id);
    }
}
