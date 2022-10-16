package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogEntityTagRel;
import io.datavines.server.repository.mapper.CatalogEntityTagRelMapper;
import io.datavines.server.repository.service.CatalogEntityTagRelService;
import org.springframework.stereotype.Service;

@Service("catalogEntityTagRelService")
public class CatalogEntityTagRelServiceImpl extends ServiceImpl<CatalogEntityTagRelMapper, CatalogEntityTagRel> implements CatalogEntityTagRelService {

}
