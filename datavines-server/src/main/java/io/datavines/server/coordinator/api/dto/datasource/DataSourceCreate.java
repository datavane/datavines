package io.datavines.server.coordinator.api.dto.datasource;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "DataSource Create cannot be null")
public class DataSourceCreate {

    @NotBlank(message = "DataSource name cannot be empty")
    private String name;

    @NotBlank(message = "DataSource type cannot be empty")
    private String type;

    @NotBlank(message = "DataSource param cannot be empty")
    private String param;
}
