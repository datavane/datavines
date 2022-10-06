package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogEntityRel;
import io.datavines.server.repository.mapper.CatalogEntityRelMapper;
import io.datavines.server.repository.service.CatalogEntityRelService;
import org.springframework.stereotype.Service;

@Service("catalogEntityRelService")
public class CatalogEntityRelServiceImpl extends ServiceImpl<CatalogEntityRelMapper, CatalogEntityRel> implements CatalogEntityRelService {

}
