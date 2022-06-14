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
    /**
     *
     */
    private String type;

    /**
     * receiver config like config email address
     */
    private String config;

    /**
     * notification config like config email is receiver or copy
     */
    private String notificationConfig;
}
