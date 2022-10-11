package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.common.datasource.jdbc.entity.ColumnInfo;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.*;
import io.datavines.server.api.dto.bo.catalog.OptionItem;
import io.datavines.server.api.dto.bo.job.JobCreateWithEntityUuid;
import io.datavines.server.api.dto.vo.*;
import io.datavines.server.repository.entity.catalog.CatalogEntityInstance;
import io.datavines.server.repository.entity.catalog.CatalogEntityMetricJobRel;
import io.datavines.server.repository.entity.catalog.CatalogEntityRel;
import io.datavines.server.repository.mapper.CatalogEntityInstanceMapper;
import io.datavines.server.repository.mapper.CatalogEntityMetricJobRelMapper;
import io.datavines.server.repository.mapper.CatalogEntityRelMapper;
import io.datavines.server.repository.service.CatalogEntityInstanceService;
import io.datavines.server.repository.service.CatalogEntityMetricJobRelService;
import io.datavines.server.repository.service.JobService;
import io.datavines.server.utils.ContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private CatalogEntityMetricJobRelService catalogEntityMetricJobRelService;

    @Autowired
    private CatalogEntityMetricJobRelMapper catalogEntityMetricJobRelMapper;

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
            List<CatalogEntityInstance> columnList = getCatalogEntityInstances(upstreamId);
            table.setColumns(CollectionUtils.isEmpty(columnList)? 0 : columnList.size());
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
        detail.setTables(CollectionUtils.isEmpty(tableList)? 0 : tableList.size());

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
        detail.setColumns(CollectionUtils.isEmpty(columnList)? 0 : columnList.size());
        detail.setComment(databaseInstance.getDescription());

        return detail;
    }

    private CatalogEntityInstance getCatalogEntityInstance(String uuid) {
        CatalogEntityInstance databaseInstance = getOne(new QueryWrapper<CatalogEntityInstance>().eq("uuid", uuid));
        return databaseInstance;
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

        return detail;
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
                    parameter.setTable(values[2]);
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

        return entityMetricPage;
    }
}
