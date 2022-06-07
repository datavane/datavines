package io.datavines.notification.plugin.email.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@EqualsAndHashCode
@ToString
public class ReceiverConfig {
    /**
     * recipient receivers
     */
    private Set<String> toReceivers;
    /**
     * carbon copy receivers
     */
    private Set<String> ccReceivers;

}
