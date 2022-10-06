package io.datavines.server.catalog.task;

import io.datavines.server.catalog.enums.FetchType;
import io.datavines.server.repository.entity.DataSource;
import lombok.Data;

@Data
public class MetaDataFetchRequest {

    private DataSource dataSource;

    private FetchType fetchType;

    private String database;

    private String table;
}
