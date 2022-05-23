package io.datavines.notification.core.dto.bo;

import lombok.Data;

@Data
public class SlasCreate {
    private String name;
    private String description;
    private Long workSpaceId;
}
