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

package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.common.utils.JSONUtils;
import io.datavines.core.enums.Status;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.api.dto.bo.catalog.task.CatalogTaskScheduleCreateOrUpdate;
import io.datavines.server.api.dto.bo.job.schedule.MapParam;
import io.datavines.server.dqc.coordinator.quartz.CatalogTaskScheduleJob;
import io.datavines.server.dqc.coordinator.quartz.QuartzExecutors;
import io.datavines.server.dqc.coordinator.quartz.ScheduleJobInfo;
import io.datavines.server.dqc.coordinator.quartz.cron.StrategyFactory;
import io.datavines.server.dqc.coordinator.quartz.cron.FunCron;
import io.datavines.server.enums.JobScheduleType;
import io.datavines.server.enums.ScheduleJobType;
import io.datavines.server.repository.entity.DataSource;
import io.datavines.server.repository.entity.catalog.CatalogTaskSchedule;
import io.datavines.server.repository.mapper.CatalogTaskScheduleMapper;
import io.datavines.server.repository.mapper.DataSourceMapper;
import io.datavines.server.repository.service.CatalogTaskScheduleService;
import io.datavines.server.utils.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("catalogTaskScheduleService")
public class CatalogTaskScheduleServiceImpl extends ServiceImpl<CatalogTaskScheduleMapper,CatalogTaskSchedule>  implements CatalogTaskScheduleService {

    @Autowired
    private QuartzExecutors quartzExecutor;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogTaskSchedule createOrUpdate(CatalogTaskScheduleCreateOrUpdate scheduleCreateOrUpdate) throws DataVinesServerException {
        if (scheduleCreateOrUpdate.getId() != null && scheduleCreateOrUpdate.getId() != 0) {
            return update(scheduleCreateOrUpdate);
        } else {
            return create(scheduleCreateOrUpdate);
        }
    }

    private CatalogTaskSchedule create(CatalogTaskScheduleCreateOrUpdate scheduleCreateOrUpdate) throws DataVinesServerException {

        Long dataSourceId = scheduleCreateOrUpdate.getDataSourceId();
        CatalogTaskSchedule catalogTaskSchedule = baseMapper.selectOne(new QueryWrapper<CatalogTaskSchedule>().eq("datasource_id", dataSourceId));
        if (catalogTaskSchedule != null) {
            throw new DataVinesServerException(Status.CATALOG_TASK_SCHEDULE_EXIST_ERROR, catalogTaskSchedule.getId());
        }

        catalogTaskSchedule = new CatalogTaskSchedule();
        BeanUtils.copyProperties(scheduleCreateOrUpdate, catalogTaskSchedule);
        catalogTaskSchedule.setCreateBy(ContextHolder.getUserId());
        catalogTaskSchedule.setCreateTime(LocalDateTime.now());
        catalogTaskSchedule.setUpdateBy(ContextHolder.getUserId());
        catalogTaskSchedule.setUpdateTime(LocalDateTime.now());
        catalogTaskSchedule.setStatus(true);

        updateCatalogTaskScheduleParam(catalogTaskSchedule, scheduleCreateOrUpdate.getType(), scheduleCreateOrUpdate.getParam());
        DataSource dataSource = dataSourceMapper.selectById(dataSourceId);
        if (dataSource == null) {
            throw new DataVinesServerException(Status.DATASOURCE_NOT_EXIST_ERROR, dataSourceId);
        } else {

            try {
                addScheduleJob(scheduleCreateOrUpdate,catalogTaskSchedule);
            } catch (Exception e) {
                throw new DataVinesServerException(Status.ADD_QUARTZ_ERROR);
            }
        }

        if (baseMapper.insert(catalogTaskSchedule) <= 0) {
            log.info("create catalog task schedule fail : {}", catalogTaskSchedule);
            throw new DataVinesServerException(Status.CREATE_CATALOG_TASK_SCHEDULE_ERROR);
        }

        log.info("create job schedule success: datasource id : {}, cronExpression : {}",
                catalogTaskSchedule.getDataSourceId(),
                catalogTaskSchedule.getCronExpression());

        return catalogTaskSchedule;
    }

    private void addScheduleJob(CatalogTaskScheduleCreateOrUpdate scheduleCreateOrUpdate, CatalogTaskSchedule catalogTaskSchedule) throws ParseException {
        switch (JobScheduleType.of(scheduleCreateOrUpdate.getType())) {
            case CYCLE:
            case CRONTAB:
                quartzExecutor.addJob(CatalogTaskScheduleJob.class, getScheduleJobInfo(catalogTaskSchedule));
                break;
            case OFFLINE:
                break;
            default:
                throw new DataVinesServerException(Status.SCHEDULE_TYPE_NOT_VALIDATE_ERROR, scheduleCreateOrUpdate.getType());
        }
    }

    private CatalogTaskSchedule update(CatalogTaskScheduleCreateOrUpdate scheduleCreateOrUpdate) throws DataVinesServerException {
        CatalogTaskSchedule catalogTaskSchedule = getById(scheduleCreateOrUpdate.getId());
        if (catalogTaskSchedule == null) {
            throw new DataVinesServerException(Status.CATALOG_TASK_SCHEDULE_NOT_EXIST_ERROR, scheduleCreateOrUpdate.getId());
        }

        BeanUtils.copyProperties(scheduleCreateOrUpdate, catalogTaskSchedule);
        catalogTaskSchedule.setUpdateBy(ContextHolder.getUserId());
        catalogTaskSchedule.setUpdateTime(LocalDateTime.now());

        updateCatalogTaskScheduleParam(catalogTaskSchedule, scheduleCreateOrUpdate.getType(), scheduleCreateOrUpdate.getParam());

        Long dataSourceId = scheduleCreateOrUpdate.getDataSourceId();
        if (dataSourceId == null) {
            throw new DataVinesServerException(Status.DATASOURCE_NOT_EXIST_ERROR);
        }

        try {
            quartzExecutor.deleteJob(getScheduleJobInfo(catalogTaskSchedule));
            addScheduleJob(scheduleCreateOrUpdate, catalogTaskSchedule);
        } catch (Exception e) {
            throw new DataVinesServerException(Status.ADD_QUARTZ_ERROR);
        }


        if (baseMapper.updateById(catalogTaskSchedule) <= 0) {
            log.info("update catalog task schedule fail : {}", catalogTaskSchedule);
            throw new DataVinesServerException(Status.UPDATE_CATALOG_TASK_SCHEDULE_ERROR, catalogTaskSchedule.getId());
        }

        return catalogTaskSchedule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(long id) {
        CatalogTaskSchedule catalogTaskSchedule = getById(id);

        Boolean deleteJob = quartzExecutor.deleteJob(getScheduleJobInfo(catalogTaskSchedule));
        if (!deleteJob ) {
            return 0;
        }
        return baseMapper.deleteById(id);
    }

    @Override
    public CatalogTaskSchedule getByDataSourceId(Long dataSourceId) {
        return baseMapper.getByDataSourceId(dataSourceId);
    }

    @Override
    public CatalogTaskSchedule getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public  List<String> getCron(MapParam mapParam){
        List<String> listCron = new ArrayList<String>();
        FunCron api = StrategyFactory.getByType(mapParam.getCycle());
        CatalogTaskSchedule catalogTaskSchedule = new CatalogTaskSchedule();
        String result1 = JSONUtils.toJsonString(mapParam);
        catalogTaskSchedule.setParam(result1);
        String cron = api.funcDeal(catalogTaskSchedule.getParam());
        listCron.add(cron);
        return listCron;
    }

    private void updateCatalogTaskScheduleParam(CatalogTaskSchedule catalogTaskSchedule, String type, MapParam param) {
        String paramStr = JSONUtils.toJsonString(param);
        switch (JobScheduleType.of(type)){
            case CYCLE:
                if (param == null) {
                    throw new DataVinesServerException(Status.SCHEDULE_PARAMETER_IS_NULL_ERROR);
                }

                if (param.getCycle() == null) {
                    throw new DataVinesServerException(Status.SCHEDULE_PARAMETER_IS_NULL_ERROR);
                }
                catalogTaskSchedule.setStatus(true);
                catalogTaskSchedule.setParam(paramStr);
                FunCron api = StrategyFactory.getByType(param.getCycle());
                catalogTaskSchedule.setCronExpression(api.funcDeal(catalogTaskSchedule.getParam()));

                log.info("job schedule param: {}", paramStr);
                break;
            case CRONTAB:
                if (param == null) {
                    throw new DataVinesServerException(Status.SCHEDULE_PARAMETER_IS_NULL_ERROR);
                }

                Boolean isValid = quartzExecutor.isValid(param.getCrontab());
                if (!isValid) {
                    throw new DataVinesServerException(Status.SCHEDULE_CRON_IS_INVALID_ERROR, param.getCrontab());
                }
                catalogTaskSchedule.setStatus(true);
                catalogTaskSchedule.setParam(paramStr);
                catalogTaskSchedule.setCronExpression(param.getCrontab());
                break;
            case OFFLINE:
                catalogTaskSchedule.setStatus(false);
                break;
            default:
                throw new DataVinesServerException(Status.SCHEDULE_TYPE_NOT_VALIDATE_ERROR, type);
        }
    }

    private ScheduleJobInfo getScheduleJobInfo(CatalogTaskSchedule catalogTaskSchedule) {
        return new ScheduleJobInfo(
                ScheduleJobType.CATALOG,
                catalogTaskSchedule.getDataSourceId(),
                catalogTaskSchedule.getDataSourceId(),
                catalogTaskSchedule.getCronExpression(),
                catalogTaskSchedule.getStartTime(),
                catalogTaskSchedule.getEndTime());
    }
}
