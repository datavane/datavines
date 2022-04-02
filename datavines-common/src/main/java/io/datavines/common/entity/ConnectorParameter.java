package io.datavines.common.entity;

import java.util.Map;

public class ConnectorParameter {

    private String type;

    private Map<String,Object> parameters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
