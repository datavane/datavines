package io.datavines.server.api.dto.vo;

import lombok.Data;

@Data
public class MetricExecutionDashBoard {

    private String datetime;

    private Object value;

    private String type;
}
