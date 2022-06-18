package io.datavines.server.coordinator.api.entity.dto.job.schedule;

import lombok.Data;

import java.util.Map;
@Data
public class MapParam {
    private String cycle;
    private Map<String, String>  parameter;
    private String   crontab;
}
