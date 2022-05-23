package io.datavines.notification.core.dto.bo;


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
