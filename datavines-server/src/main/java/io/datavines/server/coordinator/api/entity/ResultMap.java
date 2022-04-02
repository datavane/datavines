package io.datavines.server.coordinator.api.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultMap extends HashMap<String, Object> {

    public static final String EMPTY = "";

    private int code;

    public ResultMap() {
    }

    public ResultMap success() {
        this.code = 200;
        this.put("code", this.code);
        this.put("msg", "Success");
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap fail() {
        this.code = 400;
        this.put("code", code);
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap fail(int code) {
        this.code = code;
        this.put("code", code);
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap message(String message) {
        this.put("msg", message);
        return this;
    }

    public ResultMap payload(Object object) {
        this.put("data", null == object ? EMPTY : object);
        return this;
    }

    public int getCode() {
        return code;
    }
}
