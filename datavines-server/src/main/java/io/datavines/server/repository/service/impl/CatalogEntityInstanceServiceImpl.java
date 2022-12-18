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
import io.datavines.common.datasource.jdbc.entity.ColumnInfo;
import io.datavines.common.entity.job.BaseJobParameter;
import io.datavines.common.enums.DataVinesDataType;
import io.datavines.common.enums.JobType;
import io.datavines.common.utils.*;
import io.datavines.server.api.dto.bo.catalog.OptionItem;
import io.datavines.server.api.dto.bo.job.JobCreate;
import io.datavines.server.api.dto.bo.job.JobCreateWithEntityUuid;
import io.datavines.server.api.dto.bo.job.JobUpdate;
import io.datavines.server.api.dto.vo.catalog.*;
import io.datavines.server.repository.entity.Job;
import io.datavines.server.repository.entity.catalog.*;
import io.datavines.server.repository.mapper.CatalogEntityInstanceMapper;
import io.datavines.server.repository.mapper.CatalogEntityMetricJobRelMapper;
import io.datavines.server.repository.mapper.CatalogEntityRelMapper;
import io.datavines.server.repository.service.*;
import io.datavines.server.utils.ContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service("catalogEntityInstanceService")
public class CatalogEntityInstanceServiceImpl
        extends ServiceImpl<CatalogEntityInstanceMapper, CatalogEntityInstance>
        implements CatalogEntityInstanceService {

    @Resource
    private CatalogEntityRelMapper entityRelMapper;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private CatalogEntityMetricJobRelService catalogEntityMetricJobRelService;

    @Autowired
    private CatalogEntityMetricJobRelMapper catalogEntityMetricJobRelMapper;

    @Autowired
    private CatalogEntityTagRelService catalogEntityTagRelService;

    @Autowired
    private CatalogEntityProfileService catalogEntityProfileService;

    @Override
    public String create(CatalogEntityInstance entityInstance) {
        baseMapper.insert(entityInstance);
        return entityInstance.getUuid();
    }

    @Override
    public CatalogEntityInstance getByTypeAndFQN(String type, String fqn) {
        return baseMapper.selectOne(new QueryWrapper<CatalogEntityInstance>().eq("type", type).eq("fully_qualified_name", fqn));
    }

    @Override
    public CatalogEntityInstance getByDataSourceAndFQN(Long dataSourceId, String fqn) {
        return baseMapper.selectOne(new QueryWrapper<CatalogEntityInstance>()
                .eq("datasource_id", dataSourceId)
                .eq("fully_qualified_name", fqn)
                .eq("status","active"));
    }

    @Override
    public IPage<CatalogEntityInstance> getEntityPage(String upstreamId, Integer pageNumber, Integer pageSize, String whetherMark) {
        return null;
    }

    @Override
    public boolean updateStatus(String entityUUID, String status) {
        return false;
    }

    @Override
    public List<OptionItem> getEntityList(String upstreamId) {

        List<OptionItem> result = new ArrayList<>();

        List<CatalogEntityInstance> entityInstanceList = getCatalogEntityInstances(upstreamId);

        if (CollectionUtils.isNotEmpty(entityInstanceList)) {
            entityInstanceList.forEach(item -> {
                OptionItem optionItem = new OptionItem();
                optionItem.setName(item.getDisplayName());
                optionItem.setType(item.getType());
                optionItem.setUuid(item.getUuid());
                optionItem.setStatus(item.getStatus());
                result.add(optionItem);
            });
        }

        return result;
    }

    private List<CatalogEntityInstance> getCatalogEntityInstances(String upstreamId) {
        List<CatalogEntityRel> entityRelList = entityRelMapper.selectList(new QueryWrapper<CatalogEntityRel>()
                .eq("entity1_uuid", upstreamId).eq("direction","down"));
        List<String> uuidList = new ArrayList<>();
        entityRelList.forEach(x->{
            uuidList.add(x.getEntity2Uuid());
        });

        List<CatalogEntityInstance> entityInstanceList = null;
        if (CollectionUtils.isNotEmpty(uuidList)) {
            entityInstanceList = baseMapper.selectList(new QueryWrapper<CatalogEntityInstance>()
                    .in("uuid", uuidList)
                    .orderBy(true, true, "id"));
        }

        return entityInstanceList;
    }

    @Override
    public boolean deleteEntityByUUID(String entityUUID) {
        deleteEntityInstance(Collections.singletonList(entityUUID));
        return true;
    }

    @Override
    public boolean deleteEntityByDataSourceAndFQN(Long dataSourceId, String fqn) {
        CatalogEntityInstance entityInstanceDO = getByDataSourceAndFQN(dataSourceId, fqn);
        if (entityInstanceDO == null) {
            return false;
        }
        deleteEntityInstance(Collections.singletonList(entityInstanceDO.getUuid()));
        return true;
    }

    @Override
    public boolean softDeleteEntityByDataSourceAndFQN(Long dataSourceId, String fqn) {
        CatalogEntityInstance entityInstance = getByDataSourceAndFQN(dataSourceId, fqn);
        if (entityInstance == null) {
            return false;
        }
        entityInstance.setStatus("deleted");
        baseMapper.updateById(entityInstance);
        return true;
    }

    private void deleteEntityInstance(List<String> upstreamIds){
        if (CollectionUtils.isEmpty(upstreamIds)) {
            return;
        }

        for (String upstreamId: upstreamIds) {
            List<String> entityRelList = entityRelMapper
                    .selectList(new QueryWrapper<CatalogEntityRel>().eq("entity1_uuid",upstreamId))
                    .stream()
                    .map(CatalogEntityRel::getEntity2Uuid)
                    .collect(Collectors.toList());

            baseMapper.delete(new QueryWrapper<CatalogEntityInstance>().in("uuid",upstreamIds));
            entityRelMapper.delete(new QueryWrapper<CatalogEntityRel>().in("entity1_uuid",upstreamIds));

            deleteEntityInstance(entityRelList);
        }
    }

    @Override
    public List<CatalogColumnDetailVO> getCatalogColumnWithDetailList(String upstreamId) {
        List<CatalogEntityInstance> entityInstanceList = getCatalogEntityInstances(upstreamId);
        List<CatalogColumnDetailVO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(entityInstanceList)) {
            return result;
        }

        entityInstanceList.forEach(item -> {
            CatalogColumnDetailVO column = new CatalogColumnDetailVO();
            column.setName(item.getDisplayName());
            column.setUuid(item.getUuid());
            column.setUpdateTime(item.getUpdateTime());
            if (StringUtils.isNotEmpty(item.getProperties())) {
                ColumnInfo columnInfo = JSONUtils.parseObject(item.getProperties(), ColumnInfo.class);
                if (columnInfo != null) {
                    column.setComment(columnInfo.getComment());
                    column.setType(columnInfo.getType());
                }
            }

            result.add(column);
        });

        return result;
    }

    @Override
    public List<CatalogTableDetailVO> getCatalogTableWithDetailList(String upstreamId) {
        List<CatalogEntityInstance> entityInstanceList = getCatalogEntityInstances(upstreamId);
        List<CatalogTableDetailVO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(entityInstanceList)) {
            return result;
        }

        entityInstanceList.forEach(item -> {
            CatalogTableDetailVO table = new CatalogTableDetailVO();
            table.setName(item.getDisplayName());
            table.setUuid(item.getUuid());
            table.setUpdateTime(item.getUpdateTime());
            List<CatalogEntityInstance> columnList = getCatalogEntityInstances(item.getUuid());
            table.setColumns((long)(CollectionUtils.isEmpty(columnList)? 0 : columnList.size()));

            result.add(table);
        });

        return result;
    }

    @Override
    public CatalogDatabaseDetailVO getDatabaseEntityDetail(String uuid) {
        CatalogDatabaseDetailVO detail = new CatalogDatabaseDetailVO();
        CatalogEntityInstance databaseInstance = getCatalogEntityInstance(uuid);
        if (databaseInstance == null) {
            return detail;
        }

        detail.setName(databaseInstance.getDisplayName());
        detail.setType(databaseInstance.getType());
        detail.setUuid(uuid);
        detail.setUpdateTime(databaseInstance.getUpdateTime());
        List<CatalogEntityInstance> tableList = getCatalogEntityInstances(uuid);
        detail.setTables((long)(CollectionUtils.isEmpty(tableList)? 0 : tableList.size()));
        detail.setMetrics(getEntityMetricCount(uuid));

        return detail;
    }

    @Override
    public CatalogTableDetailVO getTableEntityDetail(String uuid) {
        CatalogTableDetailVO detail = new CatalogTableDetailVO();
        CatalogEntityInstance databaseInstance = getCatalogEntityInstance(uuid);
        if (databaseInstance == null) {
            return detail;
        }

        detail.setName(databaseInstance.getDisplayName());
        detail.setType(databaseInstance.getType());
        detail.setUuid(uuid);
        detail.setUpdateTime(databaseInstance.getUpdateTime());
        List<CatalogEntityInstance> columnList = getCatalogEntityInstances(uuid);
        detail.setColumns((long)(CollectionUtils.isEmpty(columnList)? 0 : columnList.size()));
        detail.setComment(databaseInstance.getDescription());
        detail.setTags(getEntityTagCount(uuid));
        detail.setMetrics(getEntityMetricCount(uuid));

        return detail;
    }

    private CatalogEntityInstance getCatalogEntityInstance(String uuid) {

        return getOne(new QueryWrapper<CatalogEntityInstance>().eq("uuid", uuid));
    }

    @Override
    public CatalogColumnDetailVO getColumnEntityDetail(String uuid) {
        CatalogColumnDetailVO detail = new CatalogColumnDetailVO();
        CatalogEntityInstance databaseInstance = getCatalogEntityInstance(uuid);
        if (databaseInstance == null) {
            return detail;
        }

        detail.setName(databaseInstance.getDisplayName());
        detail.setType(databaseInstance.getType());
        detail.setUuid(uuid);
        detail.setUpdateTime(databaseInstance.getUpdateTime());
        detail.setComment(databaseInstance.getDescription());
        detail.setTags(getEntityTagCount(uuid));
        detail.setMetrics(getEntityMetricCount(uuid));

        return detail;
    }

    @Override
    public CatalogTableProfileVO getTableEntityProfile(String uuid) {
        CatalogTableProfileVO tableProfileVO = new CatalogTableProfileVO();
        CatalogEntityInstance tableInstance = getCatalogEntityInstance(uuid);
        tableProfileVO.setUuid(uuid);
        tableProfileVO.setName(tableInstance.getDisplayName());
        tableProfileVO.setType(tableInstance.getType());
        List<CatalogEntityProfile> tableRowCounts = catalogEntityProfileService.getEntityProfileByUUIDAndMetric(uuid, "table_row_count");
        if (CollectionUtils.isEmpty(tableRowCounts)) {
            return tableProfileVO;
        }

        CatalogEntityProfile tableRecords = tableRowCounts.get(0);
        Double records = Double.valueOf(tableRecords.getActualValue());

        List<CatalogEntityInstance> columnList = getCatalogEntityInstances(uuid);
        if (CollectionUtils.isEmpty(columnList)) {
            return tableProfileVO;
        }

        List<CatalogColumnBaseProfileVO> columnBaseProfileVOS = new ArrayList<>();
        CatalogColumnBaseProfileVO columnBaseProfileVO;
        for (CatalogEntityInstance column : columnList) {
            String columnUUID = column.getUuid();
            List<CatalogEntityProfile> columnProfileList = catalogEntityProfileService.getEntityProfileByUUID(columnUUID);
            if (CollectionUtils.isEmpty(columnProfileList)) {
                continue;
            }

            columnBaseProfileVO = new CatalogColumnBaseProfileVO();
            columnBaseProfileVO.setName(column.getDisplayName());
            columnBaseProfileVO.setUuid(columnUUID);
            if (StringUtils.isNotEmpty(column.getProperties())) {
                ColumnInfo columnInfo = JSONUtils.parseObject(column.getProperties(), ColumnInfo.class);
                if (columnInfo != null) {
                    columnBaseProfileVO.setType(columnInfo.getType());
                }
            }
            for (CatalogEntityProfile entityProfile : columnProfileList) {
                String metricName = entityProfile.getMetricName();
                if (StringUtils.isEmpty(metricName)) {
                    continue;
                }

                switch (metricName) {
                    case "column_null":
                        columnBaseProfileVO.setNullCount(Long.valueOf(entityProfile.getActualValue()));
                        columnBaseProfileVO.setNullPercentage(String.format("%.2f",(Double.valueOf(entityProfile.getActualValue())/records * 100)) +"%");
                        break;
                    case "column_not_null":
                        columnBaseProfileVO.setNotNullCount(Long.valueOf(entityProfile.getActualValue()));
                        columnBaseProfileVO.setNotNullPercentage(String.format("%.2f",(Double.valueOf(entityProfile.getActualValue())/records * 100)) +"%");
                        break;
                    case "column_unique":
                        columnBaseProfileVO.setUniqueCount(Long.valueOf(entityProfile.getActualValue()));
                        columnBaseProfileVO.setUniquePercentage(String.format("%.2f",(Double.valueOf(entityProfile.getActualValue())/records * 100)) +"%");
                        break;
                    case "column_duplicate":
                        columnBaseProfileVO.setDuplicateCount(Long.valueOf(entityProfile.getActualValue()));
                        columnBaseProfileVO.setDuplicatePercentage(String.format("%.2f",(Double.valueOf(entityProfile.getActualValue())/records * 100)) +"%");
                        break;
                    case "column_histogram":
                        String histogram = entityProfile.getActualValue();
                        if (StringUtils.isNotEmpty(histogram)) {
                            List<DistributionItem> distributionItems = getDistributionItems(records, histogram);
                            columnBaseProfileVO.setTop10Distribution(distributionItems);
                        }
                        break;
                    default:
                        break;
                }
            }

            columnBaseProfileVOS.add(columnBaseProfileVO);
        }

        tableProfileVO.setColumnProfile(columnBaseProfileVOS);
        return tableProfileVO;
    }

    @Override
    public Object getColumnEntityProfile(String uuid) {
        CatalogColumnBaseProfileVO catalogColumnBaseProfileVO = new CatalogColumnBaseProfileVO();
        CatalogEntityInstance tableEntity = getParentEntity(uuid);
        if (tableEntity == null) {
            return catalogColumnBaseProfileVO;
        }
        List<CatalogEntityProfile> tableRowCounts = catalogEntityProfileService.getEntityProfileByUUIDAndMetric(tableEntity.getUuid(), "table_row_count");
        if (CollectionUtils.isEmpty(tableRowCounts)) {
            return catalogColumnBaseProfileVO;
        }

        CatalogEntityProfile tableRecords = tableRowCounts.get(0);
        Double records = Double.valueOf(tableRecords.getActualValue());
        List<CatalogEntityProfile> columnProfileList = catalogEntityProfileService.getEntityProfileByUUID(uuid);
        if (CollectionUtils.isEmpty(columnProfileList)) {
            return catalogColumnBaseProfileVO;
        }

        String columnType = "";
        CatalogEntityInstance columnEntity = getCatalogEntityInstance(uuid);
        if (StringUtils.isNotEmpty(columnEntity.getProperties())) {
            ColumnInfo columnInfo = JSONUtils.parseObject(columnEntity.getProperties(), ColumnInfo.class);
            if (columnInfo != null) {
                columnType = columnInfo.getType();
            }
        }

        DataVinesDataType dataVinesDataType = DataVinesDataType.getType(columnType);
        if (dataVinesDataType == null) {
            return catalogColumnBaseProfileVO;
        }

        switch (dataVinesDataType) {
            case STRING_TYPE:
                return getStringColumnProfile(columnEntity, columnProfileList, records);
            case NUMERIC_TYPE:
                return getNumericColumnProfile(columnEntity, columnProfileList, records);
            case DATE_TIME_TYPE:
                return getDateTimeColumnProfile(columnEntity, columnProfileList, records);
            default:
                break;
        }

        return null;
    }

    private CatalogColumnStringProfileVO getStringColumnProfile(CatalogEntityInstance columnEntity, List<CatalogEntityProfile> columnProfileList, Double records) {
        CatalogColumnStringProfileVO columnStringProfileVO = new CatalogColumnStringProfileVO();
        columnStringProfileVO.setName(columnEntity.getDisplayName());
        columnStringProfileVO.setUuid(columnEntity.getUuid());
        if (StringUtils.isNotEmpty(columnEntity.getProperties())) {
            ColumnInfo columnInfo = JSONUtils.parseObject(columnEntity.getProperties(), ColumnInfo.class);
            if (columnInfo != null) {
                columnStringProfileVO.setType(columnInfo.getType());
            }
        }
        DecimalFormat df = new DecimalFormat("#.00");
        for (CatalogEntityProfile entityProfile : columnProfileList) {
            String metricName = entityProfile.getMetricName();
            if (StringUtils.isEmpty(metricName)) {
                continue;
            }

            switch (metricName) {
                case "column_null":
                    columnStringProfileVO.setNullCount(Long.valueOf(entityProfile.getActualValue()));
                    columnStringProfileVO.setNullPercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_not_null":
                    columnStringProfileVO.setNotNullCount(Long.valueOf(entityProfile.getActualValue()));
                    columnStringProfileVO.setNotNullPercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_unique":
                    columnStringProfileVO.setUniqueCount(Long.valueOf(entityProfile.getActualValue()));
                    columnStringProfileVO.setUniquePercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_duplicate":
                    columnStringProfileVO.setDuplicateCount(Long.valueOf(entityProfile.getActualValue()));
                    columnStringProfileVO.setDuplicatePercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_histogram":
                    String histogram = entityProfile.getActualValue();
                    if (StringUtils.isNotEmpty(histogram)) {
                        List<DistributionItem> distributionItems = getDistributionItems(records, histogram);
                        columnStringProfileVO.setTop10Distribution(distributionItems);
                    }
                    break;
                case "column_blank":
                    columnStringProfileVO.setBlankCount(Long.valueOf(entityProfile.getActualValue()));
                    break;
                case "column_avg_length":
                    columnStringProfileVO.setAvgLength(df.format(Double.valueOf(entityProfile.getActualValue())));
                    break;
                case "column_max_length":
                    columnStringProfileVO.setMaxLength(df.format(Double.valueOf(entityProfile.getActualValue())));
                    break;
                case "column_max":
                    columnStringProfileVO.setMaxValue(entityProfile.getActualValue());
                    break;
                case "column_min_length":
                    columnStringProfileVO.setMinLength(df.format(Double.valueOf(entityProfile.getActualValue())));
                    break;
                case "column_min":
                    columnStringProfileVO.setMinValue(entityProfile.getActualValue());
                    break;
                default:
                    break;
            }
        }

        return columnStringProfileVO;
    }

    private List<DistributionItem> getDistributionItems(Double records, String histogram) {
        String[] values = histogram.split(",");
        List<DistributionItem> distributionItems = new ArrayList<>();
        DistributionItem item;
        int size = Math.min(10, values.length);
        for (int i = 0; i < size; i++) {
            String[] values2 = values[i].split(":");
            if (values2.length == 2) {
                item = new DistributionItem(values2[0], Long.valueOf(values2[1]), String.format("%.2f", (Double.valueOf(values2[1]) / records * 100)) + "%");
                distributionItems.add(item);
            }
        }
        return distributionItems;
    }

    private CatalogColumnNumericProfileVO getNumericColumnProfile(CatalogEntityInstance columnEntity, List<CatalogEntityProfile> columnProfileList, Double records) {
        CatalogColumnNumericProfileVO columnNumericProfileVO = new CatalogColumnNumericProfileVO();
        columnNumericProfileVO.setName(columnEntity.getDisplayName());
        columnNumericProfileVO.setUuid(columnEntity.getUuid());
        if (StringUtils.isNotEmpty(columnEntity.getProperties())) {
            ColumnInfo columnInfo = JSONUtils.parseObject(columnEntity.getProperties(), ColumnInfo.class);
            if (columnInfo != null) {
                columnNumericProfileVO.setType(columnInfo.getType());
            }
        }

        for (CatalogEntityProfile entityProfile : columnProfileList) {
            String metricName = entityProfile.getMetricName();
            if (StringUtils.isEmpty(metricName)) {
                continue;
            }

            switch (metricName) {
                case "column_null":
                    columnNumericProfileVO.setNullCount(Long.valueOf(entityProfile.getActualValue()));
                    columnNumericProfileVO.setNullPercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_not_null":
                    columnNumericProfileVO.setNotNullCount(Long.valueOf(entityProfile.getActualValue()));
                    columnNumericProfileVO.setNotNullPercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_unique":
                    columnNumericProfileVO.setUniqueCount(Long.valueOf(entityProfile.getActualValue()));
                    columnNumericProfileVO.setUniquePercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_duplicate":
                    columnNumericProfileVO.setDuplicateCount(Long.valueOf(entityProfile.getActualValue()));
                    columnNumericProfileVO.setDuplicatePercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_histogram":
                    String histogram = entityProfile.getActualValue();
                    if (StringUtils.isNotEmpty(histogram)) {
                        List<DistributionItem> distributionItems = getDistributionItems(records, histogram);
                        columnNumericProfileVO.setTop10Distribution(distributionItems);
                    }
                    break;
                case "column_max":
                    columnNumericProfileVO.setMaxValue(entityProfile.getActualValue());
                    break;
                case "column_min":
                    columnNumericProfileVO.setMinValue(entityProfile.getActualValue());
                    break;
                case "column_avg" :
                    columnNumericProfileVO.setAvgValue(entityProfile.getActualValue());
                    break;
                case "column_sum" :
                    columnNumericProfileVO.setSumValue(entityProfile.getActualValue());
                    break;
                case "column_std_dev" :
                    columnNumericProfileVO.setStdDev(entityProfile.getActualValue());
                    break;
                case "column_variance" :
                    columnNumericProfileVO.setVariance(entityProfile.getActualValue());
                    break;
                default:
                    break;
            }
        }

        return columnNumericProfileVO;
    }

    private CatalogColumnDateTimeProfileVO getDateTimeColumnProfile(CatalogEntityInstance columnEntity, List<CatalogEntityProfile> columnProfileList, Double records) {
        CatalogColumnDateTimeProfileVO columnDateTimeProfileVO = new CatalogColumnDateTimeProfileVO();
        columnDateTimeProfileVO.setName(columnEntity.getDisplayName());
        columnDateTimeProfileVO.setUuid(columnEntity.getUuid());
        if (StringUtils.isNotEmpty(columnEntity.getProperties())) {
            ColumnInfo columnInfo = JSONUtils.parseObject(columnEntity.getProperties(), ColumnInfo.class);
            if (columnInfo != null) {
                columnDateTimeProfileVO.setType(columnInfo.getType());
            }
        }

        for (CatalogEntityProfile entityProfile : columnProfileList) {
            String metricName = entityProfile.getMetricName();
            if (StringUtils.isEmpty(metricName)) {
                continue;
            }

            switch (metricName) {
                case "column_null":
                    columnDateTimeProfileVO.setNullCount(Long.valueOf(entityProfile.getActualValue()));
                    columnDateTimeProfileVO.setNullPercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_not_null":
                    columnDateTimeProfileVO.setNotNullCount(Long.valueOf(entityProfile.getActualValue()));
                    columnDateTimeProfileVO.setNotNullPercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_unique":
                    columnDateTimeProfileVO.setUniqueCount(Long.valueOf(entityProfile.getActualValue()));
                    columnDateTimeProfileVO.setUniquePercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_duplicate":
                    columnDateTimeProfileVO.setDuplicateCount(Long.valueOf(entityProfile.getActualValue()));
                    columnDateTimeProfileVO.setDuplicatePercentage(String.format("%.2f", (Double.valueOf(entityProfile.getActualValue()) / records * 100)) + "%");
                    break;
                case "column_histogram":
                    String histogram = entityProfile.getActualValue();
                    if (StringUtils.isNotEmpty(histogram)) {
                        List<DistributionItem> distributionItems = getDistributionItems(records, histogram);
                        columnDateTimeProfileVO.setTop10Distribution(distributionItems);
                    }
                    break;
                case "column_max":
                    columnDateTimeProfileVO.setMaxValue(entityProfile.getActualValue());
                    break;
                case "column_min":
                    columnDateTimeProfileVO.setMinValue(entityProfile.getActualValue());
                    break;
                default:
                    break;
            }
        }

        return columnDateTimeProfileVO;
    }

    @Override
    public long entityAddMetric(JobCreateWithEntityUuid jobCreateWithEntityUuid) {

        long jobId = jobService.create(jobCreateWithEntityUuid.getJobCreate());

        if (jobId != 0L) {
            CatalogEntityMetricJobRel entityMetricJobRel = new CatalogEntityMetricJobRel();
            entityMetricJobRel.setMetricJobId(jobId);
            entityMetricJobRel.setEntityUuid(jobCreateWithEntityUuid.getEntityUuid());
            entityMetricJobRel.setCreateBy(ContextHolder.getUserId());
            entityMetricJobRel.setUpdateBy(ContextHolder.getUserId());
            catalogEntityMetricJobRelService.save(entityMetricJobRel);

            return entityMetricJobRel.getId();
        }

        return 0;
    }

    @Override
    public CatalogEntityMetricParameter getEntityMetricParameter(String uuid) {
        CatalogEntityMetricParameter parameter = new CatalogEntityMetricParameter();

        CatalogEntityInstance entityInstance = getCatalogEntityInstance(uuid);
        if (entityInstance == null) {
            return parameter;
        }

        parameter.setDataSourceId(entityInstance.getDatasourceId());
        String[] values = entityInstance.getFullyQualifiedName().split("\\.");
        switch (entityInstance.getType()) {
            case "database":
                parameter.setDatabase(entityInstance.getDisplayName());
                break;
            case "table":
                if (values.length == 2) {
                    parameter.setDatabase(values[0]);
                    parameter.setTable(values[1]);
                }
                break;
            case "column":
                if (values.length == 3) {
                    parameter.setDatabase(values[0]);
                    parameter.setTable(values[1]);
                    parameter.setColumn(values[2]);
                }
                break;
            default:
                break;
        }

        return parameter;
    }

    @Override
    public IPage<CatalogEntityMetricVO> getEntityMetricList(String uuid, Integer pageNumber, Integer pageSize) {
        Page<CatalogEntityMetricVO> page = new Page<>(pageNumber, pageSize);
        IPage<CatalogEntityMetricVO> entityMetricPage = catalogEntityMetricJobRelMapper.getEntityMetricPage(page, uuid);
        String startTime = DateUtils.dateToString(DateUtils.getSomeDay(new Date(), -14));
        String endTime = DateUtils.dateToString(DateUtils.getSomeDay(new Date(), 1));
        entityMetricPage.getRecords().forEach(catalogEntityMetricVO -> {
            catalogEntityMetricVO.setCharts(jobExecutionService.getMetricExecutionDashBoard(catalogEntityMetricVO.getId(), startTime ,endTime));
        });

        return entityMetricPage;
    }

    @Override
    public boolean executeDataProfileJob(String uuid) {
        CatalogEntityInstance entityInstance = getCatalogEntityInstance(uuid);
        if (entityInstance == null) {
            return false;
        }

        if (!"table".equalsIgnoreCase(entityInstance.getType())) {
            return false;
        }

        List<CatalogEntityInstance> columnInstanceList = getCatalogEntityInstances(entityInstance.getUuid());

        if (CollectionUtils.isEmpty(columnInstanceList)) {
            return false;
        }

        List<BaseJobParameter> jobParameters = new ArrayList<>();
        BaseJobParameter baseJobParameter = new BaseJobParameter();
        baseJobParameter.setMetricType("table_row_count");
        Map<String,Object> metricParameter = new HashMap<>();
        String fqn = entityInstance.getFullyQualifiedName();
        if (StringUtils.isNotEmpty(fqn)) {
            String[] values = fqn.split("\\.");
            if (values.length == 2) {
                metricParameter.put("database", values[0]);
                metricParameter.put("table", values[1]);
                metricParameter.put("entity_uuid", entityInstance.getUuid());
                metricParameter.put("actual_value_type", "count");
                baseJobParameter.setMetricParameter(metricParameter);
                baseJobParameter.setExpectedType("fix_value");
                jobParameters.add(baseJobParameter);
            }
        }

        for (CatalogEntityInstance catalogEntityInstance : columnInstanceList) {
            String properties = catalogEntityInstance.getProperties();
            if (StringUtils.isEmpty(properties)) {
                continue;
            }
            Map<String,String> propertiesMap = JSONUtils.toMap(properties);

            DataVinesDataType dataVinesDataType = DataVinesDataType.getType(propertiesMap.get("type"));
            if (dataVinesDataType == null) {
                continue;
            }

            List<String> type2MetricList = dataVinesDataType.getMetricList();
            for (String metric : type2MetricList) {
                if ("true".equals(propertiesMap.get("primaryKey")) && "column_histogram".equalsIgnoreCase(metric)) {
                    continue;
                }
                baseJobParameter = new BaseJobParameter();
                baseJobParameter.setMetricType(metric);
                Map<String,Object> metricParameter1 = new HashMap<>();
                String fqn1 = catalogEntityInstance.getFullyQualifiedName();
                if (StringUtils.isEmpty(fqn1)) {
                    continue;
                }
                String[] values = fqn1.split("\\.");
                if (values.length < 3) {
                    continue;
                }
                metricParameter1.put("database", values[0]);
                metricParameter1.put("table", values[1]);
                metricParameter1.put("column", values[2]);
                metricParameter1.put("entity_uuid", catalogEntityInstance.getUuid());
                metricParameter1.put("actual_value_type", "count");
                baseJobParameter.setMetricParameter(metricParameter1);
                baseJobParameter.setExpectedType("fix_value");
                jobParameters.add(baseJobParameter);
            }
        }

        String jobName = jobService.getJobName(JobType.DATA_PROFILE.getDescription(), JSONUtils.toJsonString(jobParameters));
        Job job = jobService.getOne(new QueryWrapper<Job>().eq("name", jobName));
        if (job == null) {
            JobCreate jobCreate = new JobCreate();
            jobCreate.setType(JobType.DATA_PROFILE.getDescription());
            jobCreate.setDataSourceId(entityInstance.getDatasourceId());
            jobCreate.setParameter(JSONUtils.toJsonString(jobParameters));
            jobCreate.setRunningNow(1);
            return jobService.create(jobCreate) > 0;
        } else {
            JobUpdate jobUpdate = new JobUpdate();
            jobUpdate.setId(job.getId());
            jobUpdate.setType(JobType.DATA_PROFILE.getDescription());
            jobUpdate.setDataSourceId(entityInstance.getDatasourceId());
            jobUpdate.setParameter(JSONUtils.toJsonString(jobParameters));
            jobUpdate.setRunningNow(1);
            return jobService.update(jobUpdate) > 0;
        }
    }

    private long getEntityTagCount(String uuid) {
        return catalogEntityTagRelService.count(new QueryWrapper<CatalogEntityTagRel>().eq("entity_uuid", uuid));
    }

    private long getEntityMetricCount(String uuid) {
        return catalogEntityMetricJobRelService.count(new QueryWrapper<CatalogEntityMetricJobRel>().eq("entity_uuid", uuid));
    }

    private CatalogEntityInstance getParentEntity(String uuid) {
        List<CatalogEntityRel> entityRelList = entityRelMapper.selectList(new QueryWrapper<CatalogEntityRel>()
                .eq("entity2_uuid", uuid).eq("direction", "down"));

        if (CollectionUtils.isEmpty(entityRelList)) {
            return null;
        }

        CatalogEntityRel entityRel = entityRelList.get(0);
        String parentUUID = entityRel.getEntity1Uuid();
        return getCatalogEntityInstance(parentUUID);
    }
}
