package io.datavines.connector.plugin;

import io.datavines.connector.api.ConnectorParameterConverter;
import io.datavines.connector.api.Dialect;

public class HiveConnectorFactory extends JdbcConnectorFactory {

    @Override
    public ConnectorParameterConverter getConnectorParameterConverter() {
        return new HiveConnectorParameterConverter();
    }

    @Override
    public Dialect getDialect() {
        return new HiveDialect();
    }
}
