package io.datavines.common.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 * EnvConfig
 */
public class EnvConfig implements IConfig {

    @JsonProperty("engine")
    private String engine;

    @JsonProperty("type")
    private String type = "batch";

    @JsonProperty("config")
    private Map<String,Object> config;

    public EnvConfig() {
    }

    public EnvConfig(String engine, String type, Map<String,Object> config) {
        this.engine = engine;
        this.type = type;
        this.config = config;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public void validate() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(engine), "engine should not be empty");
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type should not be empty");
        Preconditions.checkArgument(config != null, "config should not be empty");
    }
}
