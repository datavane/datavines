package io.datavines.common.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 * BaseConfig
 */
public class BaseConfig implements IConfig {

    @JsonProperty("plugin")
    private String plugin;

    @JsonProperty("type")
    private String type;

    @JsonProperty("config")
    private Map<String,Object> config;

    public BaseConfig() {
    }

    public BaseConfig(String plugin, Map<String,Object> config) {
        this.plugin = plugin;
        this.config = config;
        this.type = "";
    }

    public BaseConfig(String plugin, String type, Map<String,Object> config) {
        this.plugin = plugin;
        this.type = type;
        this.config = config;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String type) {
        this.plugin = type;
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
        Preconditions.checkArgument(StringUtils.isNotEmpty(plugin), "plugin should not be empty");
        Preconditions.checkArgument(config != null, "config should not be empty");
    }
}
