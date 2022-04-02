package io.datavines.connector.mysql;

import io.datavines.connector.api.ConnectorParameterConverter;
import io.datavines.connector.jdbc.JdbcConnectorFactory;

/**
 * 
 */
public class MysqlConnectorFactory extends JdbcConnectorFactory {

    @Override
    public ConnectorParameterConverter getConnectorParameterConverter() {
        return new MysqlConnectorParameterConverter();
    }
}
