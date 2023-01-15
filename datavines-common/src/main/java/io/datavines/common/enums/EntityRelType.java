package io.datavines.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.HashMap;

public enum EntityRelType {

    /**
     * 0 upstream
     * 1 downstream
     * 2 child
     * 3 parent
     */
    UPSTREAM(0, "upstream"),
    DOWNSTREAM(1, "downstream"),
    CHILD(2, "child"),
    PARENT(3, "parent");

    EntityRelType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @EnumValue
    private final int code;
    private final String description;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private static final HashMap<String, EntityRelType> ENTITY_REL_TYPE_MAP = new HashMap<>();

    static {
        for (EntityRelType relType: EntityRelType.values()){
            ENTITY_REL_TYPE_MAP.put(relType.description, relType);
        }
    }

    public static EntityRelType of(String relType){
        if(ENTITY_REL_TYPE_MAP.containsKey(relType)){
            return ENTITY_REL_TYPE_MAP.get(relType);
        }
        throw new IllegalArgumentException("invalid entity rel type : " + relType);
    }
}
