package io.datavines.common.config.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.HashMap;

public enum TransformType {

    /**
     * 0 invalidate items
     * 1 actual value
     * 2 expected value from default source
     * 3 expected value from src source
     * 4 expected value from target source
     **/
    INVALIDATE_ITEMS(0, "invalidate_items"),
    ACTUAL_VALUE(1, "actual_value"),
    EXPECTED_VALUE_FROM_METADATA_SOURCE(2, "expected_value_from_metadata_source"),
    EXPECTED_VALUE_FROM_SRC_SOURCE(3, "expected_value_from_src_source"),
    EXPECTED_VALUE_FROM_TARGET_SOURCE(4, "expected_value_from_target_source");

    TransformType(int code, String description){
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

    private static final HashMap<Integer, TransformType> SINK_TYPE_MAP = new HashMap<>();

    static {
        for (TransformType sinkType: TransformType.values()){
            SINK_TYPE_MAP.put(sinkType.code,sinkType);
        }
    }

    public static TransformType of(int transform){
        if(SINK_TYPE_MAP.containsKey(transform)){
            return SINK_TYPE_MAP.get(transform);
        }
        throw new IllegalArgumentException("invalid transform type : " + transform);
    }

    public static TransformType of(String transform){

        for (TransformType transformType: TransformType.values()){
            if(transformType.getDescription().equals(transform)){
                return transformType;
            }
        }
        throw new IllegalArgumentException("invalid transform type : " + transform);
    }
}
