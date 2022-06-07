package io.datavines.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
public class SlasNotificationResultRecord {
    private static final long serialVersionUID = -1L;

    private Boolean status;

    private String message;

    public SlasNotificationResultRecord(){
        this.status = false;
    }
}
