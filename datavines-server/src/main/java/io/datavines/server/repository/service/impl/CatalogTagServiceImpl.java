package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogTag;
import io.datavines.server.repository.mapper.CatalogTagMapper;
import io.datavines.server.repository.service.CatalogTagService;
import org.springframework.stereotype.Service;

@Service("catalogTagService")
public class CatalogTagServiceImpl extends ServiceImpl<CatalogTagMapper, CatalogTag> implements CatalogTagService {

}
