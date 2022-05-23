package io.datavines.notification.core.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SlasVo {
    private Long id;
    private String name;
    private Integer jobs;
    private String updater;
    private LocalDateTime updateTime;
}
