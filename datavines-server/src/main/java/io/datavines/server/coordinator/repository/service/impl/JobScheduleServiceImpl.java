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
import io.datavines.common.utils.JSONUtils;
import io.datavines.core.enums.ApiStatus;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleCreateOrUpdate;
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

import io.datavines.server.enums.JobScheduleType;
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
@Service("jobScheduleService")
public class JobScheduleServiceImpl extends ServiceImpl<JobScheduleMapper, JobSchedule>  implements JobScheduleService {

    @Autowired
    private QuartzExecutors quartzExecutor;

    @Autowired
    private JobMapper jobMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobSchedule createOrUpdate(JobScheduleCreateOrUpdate jobScheduleCreateOrUpdate) throws DataVinesServerException {
        if (jobScheduleCreateOrUpdate.getId() != null && jobScheduleCreateOrUpdate.getId() != 0) {
            return update(jobScheduleCreateOrUpdate);
        } else {
            return create(jobScheduleCreateOrUpdate);
        }
    }

    private JobSchedule create(JobScheduleCreateOrUpdate jobScheduleCreate) throws DataVinesServerException {

        Long jobId = jobScheduleCreate.getJobId();
        JobSchedule jobSchedule = baseMapper.selectOne(new QueryWrapper<JobSchedule>().eq("job_id", jobId));
        if (jobSchedule != null) {
            throw new DataVinesServerException(ApiStatus.JOB_SCHEDULE_EXIST_ERROR, jobSchedule.getId());
        }

        jobSchedule = new JobSchedule();
        BeanUtils.copyProperties(jobScheduleCreate, jobSchedule);
        jobSchedule.setCreateBy(ContextHolder.getUserId());
        jobSchedule.setCreateTime(LocalDateTime.now());
        jobSchedule.setUpdateBy(ContextHolder.getUserId());
        jobSchedule.setUpdateTime(LocalDateTime.now());
        jobSchedule.setStatus(true);

        updateJobScheduleParam(jobSchedule, jobScheduleCreate.getType(), jobScheduleCreate.getParam());
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new DataVinesServerException(ApiStatus.JOB_NOT_EXIST_ERROR, jobId);
        } else {
            Long dataSourceId = job.getDataSourceId();
            if (dataSourceId == null) {
                throw new DataVinesServerException(ApiStatus.DATASOURCE_NOT_EXIST_ERROR);
            }
            try {
                quartzExecutor.addJob(ScheduleJob.class, job.getDataSourceId(), jobSchedule);
            } catch (Exception e) {
                throw new DataVinesServerException(ApiStatus.ADD_QUARTZ_ERROR);
            }
        }

        if (baseMapper.insert(jobSchedule) <= 0) {
            log.info("create job schedule fail : {}", jobScheduleCreate);
            throw new DataVinesServerException(ApiStatus.CREATE_JOB_SCHEDULE_ERROR);
        }
        log.info("create job schedule success: datasource id : {}, job id :{}, cronExpression : {}",
                job.getDataSourceId(),
                jobSchedule.getJobId(),
                jobSchedule.getCronExpression());

        return jobSchedule;
    }

    private JobSchedule update(JobScheduleCreateOrUpdate jobScheduleUpdate) throws DataVinesServerException {
        JobSchedule jobSchedule = getById(jobScheduleUpdate.getId());
        if (jobSchedule == null) {
            throw new DataVinesServerException(ApiStatus.JOB_SCHEDULE_NOT_EXIST_ERROR, jobScheduleUpdate.getId());
        }

        BeanUtils.copyProperties(jobScheduleUpdate, jobSchedule);
        jobSchedule.setUpdateBy(ContextHolder.getUserId());
        jobSchedule.setUpdateTime(LocalDateTime.now());

        updateJobScheduleParam(jobSchedule, jobScheduleUpdate.getType(), jobScheduleUpdate.getParam());
        Long jobId = jobScheduleUpdate.getJobId();
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new DataVinesServerException(ApiStatus.JOB_NOT_EXIST_ERROR, jobId);
        } else {
            Long dataSourceId = job.getDataSourceId();
            if (dataSourceId == null) {
                throw new DataVinesServerException(ApiStatus.DATASOURCE_NOT_EXIST_ERROR);
            }

            try {
                quartzExecutor.deleteJob(jobId, dataSourceId);
                switch (JobScheduleType.of(jobScheduleUpdate.getType())) {
                    case CYCLE:
                    case CRONTAB:
                        quartzExecutor.addJob(ScheduleJob.class, job.getDataSourceId(), jobSchedule);
                        break;
                    case NONE:
                        break;
                    default:
                        throw new DataVinesServerException(ApiStatus.JOB_SCHEDULE_TYPE_NOT_VALIDATE_ERROR, jobScheduleUpdate.getType());
                }
            } catch (Exception e) {
                throw new DataVinesServerException(ApiStatus.ADD_QUARTZ_ERROR);
            }
        }

        if (baseMapper.updateById(jobSchedule) <= 0) {
            log.info("update job schedule fail : {}", jobScheduleUpdate);
            throw new DataVinesServerException(ApiStatus.UPDATE_JOB_SCHEDULE_ERROR, jobScheduleUpdate.getId());
        }

        return jobSchedule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(long id) {
        JobSchedule jobSchedule = getById(id);
        Job job = jobMapper.selectById(jobSchedule.getJobId());
        Long jobId = job.getId();
        Long dataSourceId = job.getDataSourceId();

        Boolean deleteJob = quartzExecutor.deleteJob(jobId, dataSourceId);
        if (!deleteJob ) {
            return 0;
        }
        return baseMapper.deleteById(id);
    }

    @Override
    public JobSchedule getByJobId(Long jobId) {
        return baseMapper.getByJobId(jobId);
    }

    @Override
    public JobSchedule getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public  List<String> getCron(MapParam mapParam){
        List<String> listCron = new ArrayList<String>();
        FunCron api = StrategyFactory.getByType(mapParam.getCycle());
        JobSchedule jobSchedule = new JobSchedule();
        String result1 = JSONUtils.toJsonString(mapParam);
        jobSchedule.setParam(result1);
        String cron = api.funcDeal(jobSchedule);
        listCron.add(cron);
        return listCron;
    }

    private void updateJobScheduleParam(JobSchedule jobSchedule, String type, MapParam param) {
        String paramStr = JSONUtils.toJsonString(param);
        switch (JobScheduleType.of(type)){
            case CYCLE:
                if (param == null) {
                    throw new DataVinesServerException(ApiStatus.JOB_SCHEDULE_PARAMETER_IS_NULL_ERROR);
                }

                if (param.getCycle() == null) {
                    throw new DataVinesServerException(ApiStatus.JOB_SCHEDULE_PARAMETER_IS_NULL_ERROR);
                }
                jobSchedule.setStatus(true);
                jobSchedule.setParam(paramStr);
                FunCron api = StrategyFactory.getByType(param.getCycle());
                jobSchedule.setCronExpression(api.funcDeal(jobSchedule));

                log.info("job schedule param: {}", paramStr);
                break;
            case CRONTAB:
                if (param == null) {
                    throw new DataVinesServerException(ApiStatus.JOB_SCHEDULE_PARAMETER_IS_NULL_ERROR);
                }

                Boolean isValid = quartzExecutor.isValid(param.getCrontab());
                if (!isValid) {
                    throw new DataVinesServerException(ApiStatus.JOB_SCHEDULE_CRON_IS_INVALID_ERROR, param.getCrontab());
                }
                jobSchedule.setStatus(true);
                jobSchedule.setParam(paramStr);
                jobSchedule.setCronExpression(param.getCrontab());
                break;
            case NONE:
                jobSchedule.setStatus(false);
                break;
            default:
                throw new DataVinesServerException(ApiStatus.JOB_SCHEDULE_TYPE_NOT_VALIDATE_ERROR, type);
        }
    }
}
