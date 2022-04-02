package io.datavines.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskExecutePlatformType {
    /**
     * 资源调度平台类型
     */
    LOCAL(0,"local"),
    YARN(1,"yarn"),
    K8S(2,"k8s");

    TaskExecutePlatformType(int code, String description){
        this.code = code;
        this.description = description;
    }

    @EnumValue
    @JsonValue
    private final int code;

    private final String description;

    public static TaskExecutePlatformType of(int code){
        for(TaskExecutePlatformType taskExecutePlatformType : values()){
            if(taskExecutePlatformType.getCode() == code){
                return taskExecutePlatformType;
            }
        }
        throw new IllegalArgumentException("invalid type : " + code);
    }

    public static TaskExecutePlatformType of(String description){
        for(TaskExecutePlatformType taskExecutePlatformType : values()){
            if(taskExecutePlatformType.getDescription().equals(description)){
                return taskExecutePlatformType;
            }
        }
        throw new IllegalArgumentException("invalid type : " + description);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
