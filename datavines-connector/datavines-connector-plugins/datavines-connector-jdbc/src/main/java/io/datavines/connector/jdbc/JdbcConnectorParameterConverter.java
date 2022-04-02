package io.datavines.connector.jdbc;

import java.util.HashMap;
import java.util.Map;

import io.datavines.connector.api.ConnectorParameterConverter;

/**
 * 
 */
public class JdbcConnectorParameterConverter implements ConnectorParameterConverter {

    @Override
    public Map<String, Object> converter(Map<String, Object> parameter) {
        Map<String,Object> config = new HashMap<>();
        config.put("table",parameter.get("src_table"));
        config.put("user",parameter.get("user"));
        config.put("password", parameter.get("password"));
        config.put("url",String.format("jdbc:%s://%s:%s/%s?%s",
                parameter.get("type"),
                parameter.get("host"),
                parameter.get("port"),
                parameter.get("database"),
                parameter.get("properties")));
        return config;
    }
}
