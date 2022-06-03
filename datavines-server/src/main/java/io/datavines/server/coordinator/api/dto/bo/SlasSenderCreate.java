package io.datavines.server.coordinator.api.dto.bo;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SlasSenderCreate {

    private Long workSpaceId;

    private Long slasId;

    private String type;

    private String name;

    private String config;
}
