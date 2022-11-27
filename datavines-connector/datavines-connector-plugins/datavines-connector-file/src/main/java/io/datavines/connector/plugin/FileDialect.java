package io.datavines.connector.plugin;

import io.datavines.connector.api.Dialect;

import java.util.List;

public class FileDialect implements Dialect {

    @Override
    public String getDriver() {
        return null;
    }

    @Override
    public String getColumnPrefix() {
        return null;
    }

    @Override
    public String getColumnSuffix() {
        return null;
    }

    @Override
    public String getRegexKey() {
        return null;
    }

    @Override
    public String getNotRegexKey() {
        return null;
    }

    @Override
    public List<String> getExcludeDatabases() {
        return null;
    }
}
