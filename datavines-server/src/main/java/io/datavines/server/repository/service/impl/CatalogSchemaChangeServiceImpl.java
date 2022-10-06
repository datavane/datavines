package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogEntityRel;
import io.datavines.server.repository.entity.catalog.CatalogSchemaChange;
import io.datavines.server.repository.mapper.CatalogEntityRelMapper;
import io.datavines.server.repository.mapper.CatalogSchemaChangeMapper;
import io.datavines.server.repository.service.CatalogEntityRelService;
import io.datavines.server.repository.service.CatalogSchemaChangeService;
import org.springframework.stereotype.Service;

@Service("catalogSchemaChangeService")
public class CatalogSchemaChangeServiceImpl extends ServiceImpl<CatalogSchemaChangeMapper, CatalogSchemaChange> implements CatalogSchemaChangeService {

}
