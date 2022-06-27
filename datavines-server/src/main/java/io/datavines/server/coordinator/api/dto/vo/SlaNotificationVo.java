package io.datavines.server.coordinator.api.dto.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@ToString
public class SlaNotificationVo {

    private Long id;

    private Long slaId;

    private Long senderId;

    private String slaName;

    private String senderName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private String updateBy;
}
