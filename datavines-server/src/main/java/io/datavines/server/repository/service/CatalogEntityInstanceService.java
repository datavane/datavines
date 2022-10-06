package io.datavines.server.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.server.repository.entity.catalog.CatalogEntityInstance;

import java.util.List;
import java.util.Map;

public interface CatalogEntityInstanceService extends IService<CatalogEntityInstance> {

    String create(CatalogEntityInstance entityInstance);

    CatalogEntityInstance getByTypeAndFQN(String type, String fqn);

    CatalogEntityInstance getByDataSourceAndFQN(Long dataSourceId, String fqn);

    IPage<CatalogEntityInstance> getEntityPage(String upstreamId, Integer pageNumber, Integer pageSize, String whetherMark);

    boolean updateStatus(String entityUUID, String status);

    List<Map<String, Object>> getEntityList(String upstreamId);

    boolean deleteEntityByUUID(String entityUUID);

    boolean deleteEntityByDataSourceAndFQN(Long dataSourceId, String fqn);

    boolean softDeleteEntityByDataSourceAndFQN(Long dataSourceId, String fqn);
}
