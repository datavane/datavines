package io.datavines.server.coordinator.api.dto.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Data
public class SlasNotificationCreate {

    private String type;

    private Long workSpaceId;

    private Long slasId;

    private Long senderId;

    private Long receiverId;

    private String config;
}
