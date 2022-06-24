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
import io.datavines.core.enums.ApiStatus;
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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)

    public Long create(JobScheduleCreate jobScheduleCreateCreate) throws DataVinesServerException {
        String cron = "";
        Long jobid = 0l;

        JobSchedule jobSchedule = new JobSchedule();
        BeanUtils.copyProperties(jobScheduleCreateCreate, jobSchedule);
        String type = jobScheduleCreateCreate.getType();
        MapParam param=jobScheduleCreateCreate.getParam();

        jobSchedule.setCreateBy(ContextHolder.getUserId());
        jobSchedule.setCreateTime(LocalDateTime.now());
        jobSchedule.setUpdateBy(ContextHolder.getUserId());
        jobSchedule.setUpdateTime(LocalDateTime.now());
        jobSchedule.setStatus(true);
        jobSchedule.setStartTime(jobScheduleCreateCreate.getStartTime());
        jobSchedule.setEndTime(jobScheduleCreateCreate.getEndTime());
        if(param != null){
            String result1 = JSONUtils.toJsonString(param);
            jobSchedule.setParam(result1);
            log.info("get jobSchedule parm:{}", result1);
        }

        List<JobSchedule> jobScheduleList = baseMapper.listByDataJobId(jobSchedule.getJobId());
        if(jobScheduleList.size()>0){
            baseMapper.deleteById(jobScheduleList.get(0).getId());
        }

        if(type.equals("cycle")){
            FunCron api =  StrategyFactory.getByType(param.getCycle());
            cron = api.funcDeal(jobSchedule);
            jobSchedule.setCron_expression(cron);
        } else if (type.equals("cron")) {
            cron = param.getCrontab();
            jobSchedule.setCron_expression(cron);
        }else {
            if(jobScheduleList.size()<=0){
                return 0l;
            }
            jobSchedule.setStatus(false);
            jobSchedule.setParam("");
            baseMapper.deleteFromJobId(jobSchedule.getJobId());
            baseMapper.insert(jobSchedule);
            return jobid;
        }


        Job job = jobMapper.selectById(jobSchedule.getJobId());
        if (job == null) {
            throw new DataVinesServerException(ApiStatus.JOB_NOT_EXIST_ERROR);
        }else {
            Long envid = job.getEnv();
            if(envid == null){
                throw new DataVinesServerException(ApiStatus.DATASOURCE_NOT_EXIST_ERROR);
            }
            try{
                quartzExecutor.addJob(ScheduleJob.class, job.getDataSourceId(),  jobSchedule);
            }catch (Exception e) {
                throw new DataVinesServerException(ApiStatus.ADD_QUARTZE_ERROR);
            }

        }

        baseMapper.insert(jobSchedule);
        log.info("create jobschedule success: datasource id:{}, job id :{}, cron:{}",  job.getDataSourceId(), jobSchedule.getJobId(),cron);
        return jobid;
    }

    @Override
    public int update(JobScheduleUpdate jobScheduleUpdate) throws DataVinesServerException {
        Long id = jobScheduleUpdate.getId();
        Long jobid = jobScheduleUpdate.getJobId();
        JobSchedule jobSchedule = baseMapper.selectById(id);
        jobSchedule.setStatus(false);
        Job job = jobMapper.selectById(jobid);
        Long dataSourceID = job.getDataSourceId();
        Boolean deljob = quartzExecutor.deleteJob(id, dataSourceID);
        if(! deljob ){
            return 0;
        }
        baseMapper.updateById(jobSchedule);
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(long id) {
        JobSchedule jobSchedule = baseMapper.selectById(id);
        Job job = jobMapper.selectById(jobSchedule.getJobId());
        baseMapper.deleteById(id);
        Long jobid = job.getId();
        Long dataSourceId = job.getDataSourceId();

        Boolean deljob = quartzExecutor.deleteJob(jobid, dataSourceId);
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
    @Override
    public  List<String> getCron(MapParam mapParam){
        List<String> listCron=new ArrayList<String>();
        FunCron api =  StrategyFactory.getByType(mapParam.getCycle());
        JobSchedule jobSchedule = new JobSchedule();
        String result1 = JSONUtils.toJsonString(mapParam);
        jobSchedule.setParam(result1);
        String cron = api.funcDeal(jobSchedule);
        listCron.add(cron);
        return  listCron;
    }

}
