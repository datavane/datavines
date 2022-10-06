package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogCommand;
import io.datavines.server.repository.mapper.CatalogCommandMapper;
import io.datavines.server.repository.service.CatalogCommandService;

import org.springframework.stereotype.Service;

@Service("catalogCommandService")
public class CatalogCommandServiceImpl
        extends ServiceImpl<CatalogCommandMapper, CatalogCommand>
        implements CatalogCommandService {
    @Override
    public long insert(CatalogCommand CatalogCommand) {
        return 0;
    }

    @Override
    public int update(CatalogCommand CatalogCommand) {
        return 0;
    }

    @Override
    public CatalogCommand getById(long id) {
        return null;
    }

    @Override
    public CatalogCommand getOne() {
        return baseMapper.getOne();
    }

    @Override
    public int deleteById(long id) {
        return 0;
    }
}
