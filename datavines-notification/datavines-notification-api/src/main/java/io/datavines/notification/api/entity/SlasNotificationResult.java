package io.datavines.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode
@ToString
public class SlasNotificationResult implements Serializable {

    private static final long serialVersionUID = -1L;

    private Boolean status;

    private List<SlasNotificationResultRecord> records;

    public SlasNotificationResult(){
        this.status = false;
    }

    public SlasNotificationResult merge(SlasNotificationResult other){
        this.status = this.status && other.status;
        this.records.addAll(other.getRecords());
        return this;
    }
}
