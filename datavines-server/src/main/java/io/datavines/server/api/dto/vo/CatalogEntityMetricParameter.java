package io.datavines.server.api.dto.vo;

import lombok.Data;

@Data
public class CatalogEntityMetricParameter {

    private Long dataSourceId;

    private String database;

    private String table;

    private String column;
}
