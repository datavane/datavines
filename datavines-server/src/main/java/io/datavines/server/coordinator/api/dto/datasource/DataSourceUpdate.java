package io.datavines.server.coordinator.api.dto.datasource;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NotNull(message = "DataSource Update cannot be null")
public class DataSourceUpdate extends DataSourceCreate {

    @NotNull(message = "DataSource id cannot be null")
    private Long id;
}
