package io.datavines.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@ToString
public class SlasNotificationMessage implements Serializable {

    private Long slasId;

    private String subject;

    private String message;
}
