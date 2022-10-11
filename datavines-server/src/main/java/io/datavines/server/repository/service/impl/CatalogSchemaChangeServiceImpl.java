package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogEntityRel;
import io.datavines.server.repository.entity.catalog.CatalogSchemaChange;
import io.datavines.server.repository.mapper.CatalogEntityRelMapper;
import io.datavines.server.repository.mapper.CatalogSchemaChangeMapper;
import io.datavines.server.repository.service.CatalogEntityRelService;
import io.datavines.server.repository.service.CatalogSchemaChangeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("catalogSchemaChangeService")
public class CatalogSchemaChangeServiceImpl extends ServiceImpl<CatalogSchemaChangeMapper, CatalogSchemaChange> implements CatalogSchemaChangeService {

    @Override
    public List<CatalogSchemaChange> getSchemaChangeList(String uuid) {
        return baseMapper.selectList(new QueryWrapper<CatalogSchemaChange>().eq("entity_uuid", uuid));
    }
}
