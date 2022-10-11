package io.datavines.server.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.server.api.dto.bo.catalog.OptionItem;
import io.datavines.server.api.dto.bo.job.JobCreateWithEntityUuid;
import io.datavines.server.api.dto.vo.*;
import io.datavines.server.repository.entity.catalog.CatalogEntityInstance;

import java.util.List;

public interface CatalogEntityInstanceService extends IService<CatalogEntityInstance> {

    String create(CatalogEntityInstance entityInstance);

    CatalogEntityInstance getByTypeAndFQN(String type, String fqn);

    CatalogEntityInstance getByDataSourceAndFQN(Long dataSourceId, String fqn);

    IPage<CatalogEntityInstance> getEntityPage(String upstreamId, Integer pageNumber, Integer pageSize, String whetherMark);

    boolean updateStatus(String entityUUID, String status);

    List<OptionItem> getEntityList(String upstreamId);

    boolean deleteEntityByUUID(String entityUUID);

    boolean deleteEntityByDataSourceAndFQN(Long dataSourceId, String fqn);

    boolean softDeleteEntityByDataSourceAndFQN(Long dataSourceId, String fqn);

    List<CatalogColumnDetailVO> getCatalogColumnWithDetailList(String upstreamId);

    List<CatalogTableDetailVO> getCatalogTableWithDetailList(String upstreamId);

    CatalogDatabaseDetailVO getDatabaseEntityDetail(String uuid);

    CatalogTableDetailVO getTableEntityDetail(String uuid);

    CatalogColumnDetailVO getColumnEntityDetail(String uuid);

    long entityAddMetric(JobCreateWithEntityUuid jobCreateWithEntityUuid);

    CatalogEntityMetricParameter getEntityMetricParameter(String uuid);

    IPage<CatalogEntityMetricVO> getEntityMetricList(String uuid, Integer pageNumber, Integer pageSize);
}
