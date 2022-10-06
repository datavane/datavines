package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogEntityInstance;
import io.datavines.server.repository.entity.catalog.CatalogEntityRel;
import io.datavines.server.repository.mapper.CatalogEntityInstanceMapper;
import io.datavines.server.repository.mapper.CatalogEntityRelMapper;
import io.datavines.server.repository.service.CatalogEntityInstanceService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("catalogEntityInstanceService")
public class CatalogEntityInstanceServiceImpl
        extends ServiceImpl<CatalogEntityInstanceMapper, CatalogEntityInstance>
        implements CatalogEntityInstanceService {

    @Resource
    private CatalogEntityRelMapper entityRelMapper;

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
    public List<Map<String, Object>> getEntityList(String upstreamId) {
        return null;
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

}
