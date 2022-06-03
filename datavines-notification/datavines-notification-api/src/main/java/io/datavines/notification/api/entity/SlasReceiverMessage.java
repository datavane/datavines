package io.datavines.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@ToString
public class SlasReceiverMessage implements Serializable {
    private static final long serialVersionUID = -1L;

    private Long id;

    private Long type;

    private String config;
}
