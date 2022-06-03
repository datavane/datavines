package io.datavines.server.coordinator.api.dto.bo;

import lombok.Data;

@Data
public class SlasCreate {
    private String name;
    private String description;
    private Long workSpaceId;
}
