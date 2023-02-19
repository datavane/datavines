package io.datavines.server.catalog.metadata;

import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.common.utils.NetUtils;
import io.datavines.server.repository.entity.catalog.CatalogMetaDataFetchTask;
import io.datavines.server.repository.service.CatalogMetaDataFetchTaskService;
import io.datavines.server.utils.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Slf4j
public class MetaDataFetchTaskFailover {

    private CatalogMetaDataFetchTaskService metaDataFetchTaskService;

    private CatalogMetaDataFetchTaskManager metaDataFetchTaskManager;

    public MetaDataFetchTaskFailover(CatalogMetaDataFetchTaskManager metaDataFetchTaskManager) {
        this.metaDataFetchTaskService = SpringApplicationContext.getBean(CatalogMetaDataFetchTaskService.class);
        this.metaDataFetchTaskManager = metaDataFetchTaskManager;
    }

    public void handleMetaDataFetchTaskFailover(String host) {
        List<CatalogMetaDataFetchTask> needFailoverTaskList = metaDataFetchTaskService.listNeedFailover(host);
        innerHandleMetaDataFetchTaskFailover(needFailoverTaskList);
    }

    private void innerHandleMetaDataFetchTaskFailover(List<CatalogMetaDataFetchTask> needFailover) {
        if (CollectionUtils.isNotEmpty(needFailover)) {
            needFailover.forEach(task -> {
                task.setExecuteHost(NetUtils.getAddr(
                        CommonPropertyUtils.getInt(CommonPropertyUtils.SERVER_PORT, CommonPropertyUtils.SERVER_PORT_DEFAULT)));
                metaDataFetchTaskService.updateById(task);

                try {
                    metaDataFetchTaskManager.putCatalogTask(task);
                } catch (Exception e) {
                    log.error("put the task need failover into manager error : {}", e);
                }
            });
        }
    }

    public void handleMetaDataFetchTaskFailover(List<String> hostList) {
        List<CatalogMetaDataFetchTask> needFailoverTaskList = metaDataFetchTaskService.listTaskNotInServerList(hostList);
        innerHandleMetaDataFetchTaskFailover(needFailoverTaskList);
    }
}
