package io.datavines.server.coordinator.repository.service;

import io.datavines.common.exception.DataVinesException;
import io.datavines.common.param.TestConnectionRequestParam;
import io.datavines.server.coordinator.api.dto.datasource.DataSourceCreate;
import io.datavines.server.coordinator.api.dto.datasource.DataSourceUpdate;
import io.datavines.server.coordinator.repository.entity.DataSource;

import java.util.List;

public interface DataSourceService {

    boolean testConnect(TestConnectionRequestParam connectionParam);

    long insert(DataSourceCreate dataSource);

    int update(DataSourceUpdate dataSource) throws DataVinesException;

    DataSource getById(long id);

    int delete(long id);

    List<DataSource> listByWorkSpaceId(long workspaceId);

    Object getDatabaseList(Long id);

    Object getTableList(Long id, String database);

    Object getColumnList(Long id, String database, String table);
}
