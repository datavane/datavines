package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogEntityMetricJobRel;
import io.datavines.server.repository.mapper.CatalogEntityMetricJobRelMapper;
import io.datavines.server.repository.service.CatalogEntityMetricJobRelService;
import org.springframework.stereotype.Service;

@Service("catalogEntityMetricJobRelService")
public class CatalogEntityMetricJobRelServiceImpl
        extends ServiceImpl<CatalogEntityMetricJobRelMapper, CatalogEntityMetricJobRel>
        implements CatalogEntityMetricJobRelService {

}
