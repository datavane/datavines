package io.datavines.server.api.dto.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CatalogEntityBaseDetailVO implements Serializable {

    private String name;

    private String uuid;

    private String type;

    private int metrics;

    private int usages;

    private int tags;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime updateTime;
}
