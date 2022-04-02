package io.datavines.connector.jdbc;

import io.datavines.connector.api.Connector;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.connector.api.ConnectorParameterConverter;
import io.datavines.connector.api.Dialect;
import io.datavines.connector.api.ResponseConverter;

public class JdbcConnectorFactory implements ConnectorFactory {

    @Override
    public Connector getConnector() {
        return new JdbcConnector();
    }

    @Override
    public ResponseConverter getResponseConvert() {
        return new JdbcResponseConverter();
    }

    @Override
    public Dialect getDialect() {
        return new JdbcDialect();
    }

    @Override
    public ConnectorParameterConverter getConnectorParameterConverter() {
        return new JdbcConnectorParameterConverter();
    }
}
