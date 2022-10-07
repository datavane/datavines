package io.datavines.server.api.dto.bo.catalog;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "CatalogRefresh cannot be null")
public class CatalogRefresh {

    @NotBlank(message = "DataSourceId cannot be empty")
    private Long datasourceId;

    private String database;

    private String table;
}
