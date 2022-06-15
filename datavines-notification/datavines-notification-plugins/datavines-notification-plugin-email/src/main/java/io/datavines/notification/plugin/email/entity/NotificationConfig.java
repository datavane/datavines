package io.datavines.notification.plugin.email.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public class NotificationConfig {
    private String receiverType;
}
