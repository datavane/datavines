package io.datavines.connector.api;

import java.util.Map;

/**
 * 
 */
public interface ConnectorParameterConverter {

    Map<String,Object> converter(Map<String,Object> parameter);
}
