package io.datavines.connector.plugin;

import io.datavines.connector.api.ConnectorParameterConverter;
import io.datavines.connector.api.Dialect;

public class MysqlConnectorFactory extends JdbcConnectorFactory {

    @Override
    public ConnectorParameterConverter getConnectorParameterConverter() {
        return new MysqlConnectorParameterConverter();
    }

    @Override
    public Dialect getDialect() {
        return new MysqlDialect();
    }
}
