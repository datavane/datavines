package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.common.utils.JSONUtils;
import io.datavines.server.api.dto.bo.catalog.CatalogRefresh;
import io.datavines.server.registry.RegistryHolder;
import io.datavines.server.repository.entity.catalog.CatalogCommand;
import io.datavines.server.repository.entity.catalog.CatalogTask;
import io.datavines.server.repository.mapper.CatalogTaskMapper;
import io.datavines.server.repository.service.CatalogCommandService;
import io.datavines.server.repository.service.CatalogTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("catalogTaskService")
public class CatalogTaskServiceImpl
        extends ServiceImpl<CatalogTaskMapper, CatalogTask>
        implements CatalogTaskService {

    @Autowired
    private CatalogCommandService catalogCommandService;

    @Autowired
    private RegistryHolder registryHolder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long refreshCatalog(CatalogRefresh catalogRefresh) {

        Long taskId = 0L;
        registryHolder.blockUtilAcquireLock("1028");
        List<CatalogTask> oldTaskList =
                baseMapper.selectList(new QueryWrapper<CatalogTask>()
                        .eq("status",0)
                        .eq("datasource_id", catalogRefresh.getDatasourceId())
                        .eq("parameter", JSONUtils.toJsonString(catalogRefresh)));
        if (CollectionUtils.isNotEmpty(oldTaskList)) {
            registryHolder.release("1028");
            return 0L;
        }
        //生成任务之前需要检查是否有相同的任务在执行
        LocalDateTime now = LocalDateTime.now();
        CatalogTask catalogTask = new CatalogTask();
        catalogTask.setParameter(JSONUtils.toJsonString(catalogRefresh));
        catalogTask.setDataSourceId(catalogRefresh.getDatasourceId());
        catalogTask.setStatus(0);
        catalogTask.setSubmitTime(now);
        catalogTask.setCreateTime(now);
        catalogTask.setUpdateTime(now);

        baseMapper.insert(catalogTask);

        CatalogCommand catalogCommand = new CatalogCommand();
        catalogCommand.setTaskId(catalogTask.getId());
        catalogCommand.setCreateTime(now);
        catalogCommand.setUpdateTime(now);
        catalogCommandService.create(catalogCommand);
        taskId = catalogTask.getId();
        registryHolder.release("1028");

        return taskId;
    }

    @Override
    public int update(CatalogTask catalogTask) {
        return baseMapper.updateById(catalogTask);
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
