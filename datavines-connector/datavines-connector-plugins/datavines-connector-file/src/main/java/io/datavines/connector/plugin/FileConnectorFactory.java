package io.datavines.connector.plugin;

import io.datavines.connector.api.*;

public class FileConnectorFactory implements ConnectorFactory {

    @Override
    public String getCategory() {
        return "file";
    }

    @Override
    public Connector getConnector() {
        return new FileConnector();
    }

    @Override
    public ResponseConverter getResponseConvert() {
        return new FileResponseConverter();
    }

    @Override
    public Dialect getDialect() {
        return new FileDialect();
    }

    @Override
    public ConnectorParameterConverter getConnectorParameterConverter() {
        return new FileConnectorParameterConverter();
    }

    @Override
    public Executor getExecutor() {
        return new FileExecutor();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return new FileTypeConverter();
    }
}
