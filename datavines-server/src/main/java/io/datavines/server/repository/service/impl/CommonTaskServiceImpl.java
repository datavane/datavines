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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.NetUtils;
import io.datavines.common.utils.StringUtils;
import io.datavines.core.enums.Status;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.api.dto.bo.catalog.CatalogRefresh;
import io.datavines.server.api.dto.vo.catalog.CatalogMetaDataFetchTaskVO;
import io.datavines.server.enums.CommonTaskType;
import io.datavines.server.enums.FetchType;
import io.datavines.server.registry.RegistryHolder;
import io.datavines.server.repository.entity.CommonTaskCommand;
import io.datavines.server.repository.entity.CommonTask;
import io.datavines.server.repository.mapper.CommonTaskMapper;
import io.datavines.server.repository.service.CommonTaskCommandService;
import io.datavines.server.repository.service.CommonTaskScheduleService;
import io.datavines.server.repository.service.CommonTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.datavines.common.ConfigConstants.DATABASE;
import static io.datavines.common.ConfigConstants.TABLE;

@Service("commonTaskService")
public class CommonTaskServiceImpl
        extends ServiceImpl<CommonTaskMapper, CommonTask>
        implements CommonTaskService {

    @Autowired
    private CommonTaskCommandService commonTaskCommandService;

    @Autowired
    private CommonTaskScheduleService commonTaskScheduleService;

    @Autowired
    private RegistryHolder registryHolder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long refreshCatalog(CatalogRefresh catalogRefresh) {

        Long taskId = 0L;
        try {
            registryHolder.blockUtilAcquireLock("1028");
            QueryWrapper<CommonTask> queryWrapper = new QueryWrapper<>();

            queryWrapper.lambda().eq(CommonTask::getStatus,0)
                    .eq(CommonTask::getTaskType, CommonTaskType.CATALOG_METADATA_FETCH)
                    .eq(CommonTask::getDataSourceId, catalogRefresh.getDatasourceId())
                    .eq(CommonTask::getParameter, JSONUtils.toJsonString(catalogRefresh));
            List<CommonTask> oldTaskList = baseMapper.selectList(queryWrapper);

            if (CollectionUtils.isNotEmpty(oldTaskList)) {
                registryHolder.release("1028");
                return 0L;
            }
            //生成任务之前需要检查是否有相同的任务在执行
            LocalDateTime now = LocalDateTime.now();
            CommonTask commonTask = new CommonTask();
            commonTask.setTaskType(catalogRefresh.getTaskType());
            commonTask.setParameter(JSONUtils.toJsonString(catalogRefresh));
            commonTask.setDataSourceId(catalogRefresh.getDatasourceId());
            commonTask.setStatus(0);
            commonTask.setExecuteHost(NetUtils.getAddr(
                    CommonPropertyUtils.getInt(CommonPropertyUtils.SERVER_PORT, CommonPropertyUtils.SERVER_PORT_DEFAULT)));
            commonTask.setTaskType(catalogRefresh.getTaskType());
            String parameter = commonTask.getParameter();
            if (StringUtils.isNotEmpty(parameter)) {
                Map<String, String> parameterMap = JSONUtils.toMap(parameter);
                if (parameterMap != null) {
                    String database = parameterMap.get(DATABASE);
                    String table = parameterMap.get(TABLE);

                    if (StringUtils.isEmpty(database) && StringUtils.isEmpty(table)) {
                        commonTask.setType(FetchType.DATASOURCE);
                    }

                    if (StringUtils.isEmpty(database) && StringUtils.isNotEmpty(table)) {
                        throw new DataVinesServerException(Status.CATALOG_FETCH_METADATA_PARAMETER_ERROR);
                    }

                    if (StringUtils.isNotEmpty(database) && StringUtils.isEmpty(table)) {
                        commonTask.setDatabaseName(database);
                        commonTask.setType(FetchType.DATABASE);
                    }

                    if (StringUtils.isNotEmpty(database) && StringUtils.isNotEmpty(table)) {
                        commonTask.setTableName(table);
                        commonTask.setDatabaseName(database);
                        commonTask.setType(FetchType.TABLE);
                    }
                }
            }

            commonTask.setSubmitTime(now);
            commonTask.setCreateTime(now);
            commonTask.setUpdateTime(now);

            baseMapper.insert(commonTask);

            CommonTaskCommand commonTaskCommand = new CommonTaskCommand();
            commonTaskCommand.setTaskId(commonTask.getId());
            commonTaskCommand.setCreateTime(now);
            commonTaskCommand.setUpdateTime(now);
            commonTaskCommandService.create(commonTaskCommand);
            taskId = commonTask.getId();
        } finally {
            registryHolder.release("1028");
        }

        return taskId;
    }

    @Override
    public int update(CommonTask commonTask) {
        return baseMapper.updateById(commonTask);
    }

    @Override
    public CommonTask getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Long killCatalogTask(Long catalogTaskId) {
        return null;
    }

    @Override
    public List<CommonTask> listNeedFailover(String host) {
        return baseMapper.selectList(new QueryWrapper<CommonTask>().lambda()
                .eq(CommonTask::getExecuteHost, host)
                .in(CommonTask::getStatus, ExecutionStatus.RUNNING_EXECUTION.getCode(), ExecutionStatus.SUBMITTED_SUCCESS.getCode()));
    }

    @Override
    public List<CommonTask> listTaskNotInServerList(List<String> hostList) {
        return baseMapper.selectList(new QueryWrapper<CommonTask>().lambda()
                .notIn(CommonTask::getExecuteHost, hostList)
                .in(CommonTask::getStatus,ExecutionStatus.RUNNING_EXECUTION.getCode(), ExecutionStatus.SUBMITTED_SUCCESS.getCode()));
    }

    @Override
    public String getTaskExecuteHost(Long catalogTaskId) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByDataSourceId(long dataSourceId) {
        remove(new QueryWrapper<CommonTask>().lambda().eq(CommonTask::getDataSourceId, dataSourceId));
        commonTaskScheduleService.deleteByDataSourceId(dataSourceId);
        return false;
    }

    @Override
    public LocalDateTime getRefreshTime(long dataSourceId, String databaseName, String tableName) {

        CommonTask task = null;
        QueryWrapper<CommonTask> queryWrapper = new QueryWrapper<>();
        LocalDateTime refreshTime = null;

        queryWrapper.lambda().eq(CommonTask::getDataSourceId,dataSourceId)
                .eq(CommonTask::getType,FetchType.DATASOURCE)
                .orderByDesc(CommonTask::getCreateTime).last("limit 1");
        task = getOne(queryWrapper);
        if (task != null) {
            refreshTime = task.getCreateTime();
        }

        if (StringUtils.isNotEmpty(databaseName)) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(CommonTask::getDataSourceId,dataSourceId)
                    .eq(CommonTask::getDatabaseName, databaseName)
                    .eq(CommonTask::getType,FetchType.DATABASE)
                    .orderByDesc(CommonTask::getCreateTime).last("limit 1");
            task = getOne(queryWrapper);
            if (task != null) {
                if (refreshTime == null || task.getCreateTime().isAfter(refreshTime)) {
                    refreshTime = task.getCreateTime();
                }
            }
        }

        if (StringUtils.isNotEmpty(databaseName) && StringUtils.isNotEmpty(tableName)) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(CommonTask::getDataSourceId,dataSourceId)
                    .eq(CommonTask::getDatabaseName, databaseName)
                    .eq(CommonTask::getTableName,tableName)
                    .orderByDesc(CommonTask::getCreateTime).last("limit 1");
            task = getOne(queryWrapper);
            if (task != null) {
                if (refreshTime == null || task.getCreateTime().isAfter(refreshTime)) {
                    refreshTime = task.getCreateTime();
                }
            }
        }

        return refreshTime;
    }

    @Override
    public IPage<CatalogMetaDataFetchTaskVO> getFetchTaskPage(Long datasourceId, String taskType, Integer pageNumber, Integer pageSize) {
        Page<CatalogMetaDataFetchTaskVO> page = new Page<>(pageNumber, pageSize);
        return baseMapper.getJobExecutionPage(page, datasourceId, taskType);
    }
}
