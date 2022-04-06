package io.datavines.connector.plugin;

import io.datavines.connector.api.Dialect;

public abstract class JdbcDialect implements Dialect {

    @Override
    public String getColumnPrefix() {
        return "`";
    }

    @Override
    public String getColumnSuffix() {
        return "`";
    }
}
