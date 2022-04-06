package io.datavines.connector.plugin;

import io.datavines.connector.api.Connector;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.connector.api.ConnectorParameterConverter;
import io.datavines.connector.api.Dialect;
import io.datavines.connector.api.ResponseConverter;

public abstract class JdbcConnectorFactory implements ConnectorFactory {

    @Override
    public String getCategory() {
        return "jdbc";
    }

    @Override
    public Connector getConnector() {
        return new JdbcConnector();
    }

    @Override
    public ResponseConverter getResponseConvert() {
        return new JdbcResponseConverter();
    }


    @Override
    public ConnectorParameterConverter getConnectorParameterConverter() {
        return new JdbcConnectorParameterConverter();
    }
}
