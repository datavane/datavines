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
import io.datavines.common.utils.JSONUtils;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleCreate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleUpdate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.MapParam;
import io.datavines.server.coordinator.repository.entity.Job;
import io.datavines.server.coordinator.repository.entity.JobSchedule;
import io.datavines.server.coordinator.repository.mapper.JobMapper;
import io.datavines.server.coordinator.repository.mapper.JobScheduleMapper;
import io.datavines.server.coordinator.repository.service.JobScheduleService;
import io.datavines.server.coordinator.server.quartz.QuartzExecutors;
import io.datavines.server.coordinator.server.quartz.ScheduleJob;
import io.datavines.server.coordinator.server.quartz.StrategyFactory;
import io.datavines.server.coordinator.server.quartz.cron.FunCron;

import io.datavines.server.utils.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("JobScheduleService")
public class JobScheduleServiceImpl extends ServiceImpl<JobScheduleMapper, JobSchedule>  implements JobScheduleService {

    @Autowired
    private QuartzExecutors quartzExecutor;

    @Autowired
    private JobMapper jobMapper;
    @Override
    public List<String> create(JobScheduleCreate jobScheduleCreateCreate) throws DataVinesServerException {
        String cron = "";
        List<String> listCron=new ArrayList<String>();
        try {
            JobSchedule jobSchedule = new JobSchedule();
            BeanUtils.copyProperties(jobScheduleCreateCreate, jobSchedule);
            String type = jobScheduleCreateCreate.getType();
            MapParam param=jobScheduleCreateCreate.getParam();
            String result1 = JSONUtils.toJsonString(param);
            jobSchedule.setParam(result1);

            jobSchedule.setCreateBy(ContextHolder.getUserId());
            jobSchedule.setCreateTime(LocalDateTime.now());
            jobSchedule.setUpdateBy(ContextHolder.getUserId());
            jobSchedule.setUpdateTime(LocalDateTime.now());
            jobSchedule.setStatus(true);
            jobSchedule.setStartTime(jobScheduleCreateCreate.getStartTime());
            jobSchedule.setEndTime(jobScheduleCreateCreate.getEndTime());
            log.info("get jobSchedule parm:{}", result1);
            if(type.equals("cycle")){
                FunCron api =  StrategyFactory.getByType(param.getCycle());
                cron = api.funcDeal(jobSchedule);
                jobSchedule.setCron_expression(cron);
            } else if (type.equals("cron")) {
                cron = param.getCycle();
                jobSchedule.setCron_expression(cron);
            }else {
                jobSchedule.setStatus(false);
                baseMapper.insert(jobSchedule);
                return listCron;
            }

            List<JobSchedule> jobScheduleList = baseMapper.listByDataJobId(jobSchedule.getJobId());
            if(jobScheduleList.size()>0){
                baseMapper.deleteById(jobScheduleList.get(0).getId());
            }
            Job job = jobMapper.selectById(jobSchedule.getJobId());
            if (job == null) {
                quartzExecutor.addJob(ScheduleJob.class, 1,  jobSchedule);
            }else {
                quartzExecutor.addJob(ScheduleJob.class, job.getDataSourceId(),  jobSchedule);
            }

            baseMapper.insert(jobSchedule);
            listCron.add(cron);
            log.info("create jobschedule success: datasource id:{}, job id :{}",  job.getDataSourceId(), jobSchedule.getJobId());


        } catch (Exception e) {
            e.printStackTrace();
        }

        return listCron;
    }

    @Override
    public int update(JobScheduleUpdate jobScheduleUpdate) throws DataVinesServerException {
        Long id = jobScheduleUpdate.getId();
        JobSchedule jobSchedule = baseMapper.selectById(id);
        jobSchedule.setStatus(false);
        Boolean deljob = quartzExecutor.deleteJob(id, 1);
        if(! deljob ){
            return 0;
        }
        baseMapper.updateById(jobSchedule);
        return 0;
    }

    @Override
    public int deleteById(long id) {
        Boolean deljob = quartzExecutor.deleteJob(id, 1);
        if(! deljob ){
            return 0;
        }
        return baseMapper.deleteById(id);
    }

    @Override
    public List<JobSchedule> listByJobId(Long jobId) {
        List<JobSchedule> jobScheduleList = baseMapper.listByDataJobId(jobId);
        return jobScheduleList;
    }

    @Override
    public JobSchedule getById(long id) {
        return baseMapper.selectById(id);
    }
}
