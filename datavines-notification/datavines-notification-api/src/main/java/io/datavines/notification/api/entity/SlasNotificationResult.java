package io.datavines.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@ToString
public class SlasNotificationResult implements Serializable {

    private static final long serialVersionUID = -1L;

    private Boolean status;

    private String message;

    public SlasNotificationResult(){
        this.status = false;
    }
}
