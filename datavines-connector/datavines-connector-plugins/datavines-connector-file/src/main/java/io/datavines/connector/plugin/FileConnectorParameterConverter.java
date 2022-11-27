package io.datavines.connector.plugin;

import io.datavines.connector.api.ConnectorParameterConverter;

import java.util.Map;

public class FileConnectorParameterConverter implements ConnectorParameterConverter {

    @Override
    public Map<String, Object> converter(Map<String, Object> parameter) {
        return parameter;
    }
}
