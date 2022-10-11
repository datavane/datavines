package io.datavines.server.api.dto.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CatalogEntityMetricVO implements Serializable {

    private Long id;

    private String name;

    private String status;

    private List<CatalogEntityMetricChartVO> charts;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime updateTime;
}
