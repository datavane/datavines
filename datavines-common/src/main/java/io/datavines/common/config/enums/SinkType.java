package io.datavines.common.config.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.HashMap;

public enum SinkType {

    /**
     * 0 ending process when some tasks failed.
     * 1 continue running when some tasks failed.
     **/
    ERROR_DATA(0, "error_data"),
    TASK_RESULT(1, "task_result"),
    ACTUAL_VALUE(2, "actual_value");

    SinkType(int code, String description){
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

    private static final HashMap<Integer, SinkType> SINK_TYPE_MAP = new HashMap<>();

    static {
        for (SinkType sinkType: SinkType.values()){
            SINK_TYPE_MAP.put(sinkType.code,sinkType);
        }
    }

    public static SinkType of(int sink){
        if(SINK_TYPE_MAP.containsKey(sink)){
            return SINK_TYPE_MAP.get(sink);
        }
        throw new IllegalArgumentException("invalid sink type : " + sink);
    }

    public static SinkType of(String sink){

        for (SinkType sinkType: SinkType.values()){
            if(sinkType.getDescription().equals(sink)){
                return sinkType;
            }
        }
        throw new IllegalArgumentException("invalid sink type : " + sink);
    }
}
