package io.datavines.engine.executor.core.enums;

/**
 * 
 */
public enum ResourceSchedulePlatformType {
    /**
     * 资源调度平台类型
     */
    LOCAL(0,"local"),
    YARN(1,"yarn"),
    K8S(2,"k8s");

    ResourceSchedulePlatformType(int code,String description){
        this.code = code;
        this.description = description;
    }

    private final int code;
    private final String description;

    public static ResourceSchedulePlatformType of(int code){
        for(ResourceSchedulePlatformType platformType : values()){
            if(platformType.getCode() == code){
                return platformType;
            }
        }
        throw new IllegalArgumentException("invalid type : " + code);
    }

    public static ResourceSchedulePlatformType of(String description){
        for(ResourceSchedulePlatformType platformType : values()){
            if(platformType.getDescription().equals(description)){
                return platformType;
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
