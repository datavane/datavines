package io.datavines.common.config.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.HashMap;

public enum SourceType {

    /**
     * 0 normal
     * 1 invalidate items
     * 2 actual value
     **/
    NORMAL(0, "normal"),
    METADATA(1, "metadata");

    SourceType(int code, String description){
        this.code = code;
        this.description = description;
    }

    @EnumValue
    final int code;

    final String description;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private static final HashMap<Integer, SourceType> SINK_TYPE_MAP = new HashMap<>();

    static {
        for (SourceType sinkType: SourceType.values()){
            SINK_TYPE_MAP.put(sinkType.code,sinkType);
        }
    }

    public static SourceType of(int source){
        if(SINK_TYPE_MAP.containsKey(source)){
            return SINK_TYPE_MAP.get(source);
        }
        throw new IllegalArgumentException("invalid source type : " + source);
    }

    public static SourceType of(String source){

        for (SourceType sinkType: SourceType.values()){
            if(sinkType.getDescription().equals(source)){
                return sinkType;
            }
        }
        throw new IllegalArgumentException("invalid source type : " + source);
    }
}
