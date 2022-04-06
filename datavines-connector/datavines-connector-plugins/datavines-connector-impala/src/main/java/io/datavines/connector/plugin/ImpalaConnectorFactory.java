package io.datavines.connector.plugin;

import io.datavines.connector.api.ConnectorParameterConverter;
import io.datavines.connector.api.Dialect;

public class ImpalaConnectorFactory extends JdbcConnectorFactory {

    @Override
    public ConnectorParameterConverter getConnectorParameterConverter() {
        return new ImpalaConnectorParameterConverter();
    }

    @Override
    public Dialect getDialect() {
        return new ImpalaDialect();
    }
}
