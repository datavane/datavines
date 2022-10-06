package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.repository.entity.catalog.CatalogCommand;
import io.datavines.server.repository.entity.catalog.CatalogTask;
import io.datavines.server.repository.mapper.CatalogCommandMapper;
import io.datavines.server.repository.mapper.CatalogTaskMapper;
import io.datavines.server.repository.service.CatalogCommandService;
import io.datavines.server.repository.service.CatalogTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("catalogTaskService")
public class CatalogTaskServiceImpl
        extends ServiceImpl<CatalogTaskMapper, CatalogTask>
        implements CatalogTaskService {

    @Override
    public long create(CatalogTask catalogTask) {
        return 0;
    }

    @Override
    public int update(CatalogTask catalogTask) {
        return 0;
    }

    @Override
    public CatalogTask getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Long killCatalogTask(Long catalogTaskId) {
        return null;
    }

    @Override
    public List<CatalogTask> listNeedFailover(String host) {
        return null;
    }

    @Override
    public List<CatalogTask> listTaskNotInServerList(List<String> hostList) {
        return null;
    }

    @Override
    public String getTaskExecuteHost(Long catalogTaskId) {
        return null;
    }
}
