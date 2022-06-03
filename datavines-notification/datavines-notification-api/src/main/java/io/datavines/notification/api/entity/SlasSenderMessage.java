package io.datavines.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@ToString
public class SlasSenderMessage implements Serializable {

    private static final long serialVersionUID = -1L;

    private Long id;

    private Long workSpaceId;

    private String type;

    private String name;

    private String config;
}
