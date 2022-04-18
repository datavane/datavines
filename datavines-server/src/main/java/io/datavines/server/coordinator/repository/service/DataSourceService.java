package io.datavines.server.coordinator.repository.service;

import io.datavines.common.param.TestConnectionRequestParam;
import io.datavines.server.coordinator.repository.entity.DataSource;

import java.util.List;

public interface DataSourceService {

    boolean testConnect(TestConnectionRequestParam connectionParam);

    long insert(DataSource dataSource);

    int update(DataSource dataSource);

    DataSource getById(long id);

    List<DataSource> listByWorkSpaceId(long workspaceId);
}
