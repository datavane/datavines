package io.datavines.server.enums;

/**
 * 
 */
public enum ScheduleStatus {

    /**
     * 0 file, 1 udf
     */
    OFFLINE(0, "offline"),
    ONLINE(1, "online");

    ScheduleStatus(int code, String description){
        this.code = code;
        this.description = description;
    }

    int code;
    String description;
}
