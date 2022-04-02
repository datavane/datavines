package io.datavines.connector.jdbc;

import io.datavines.connector.api.Dialect;

public class JdbcDialect implements Dialect {
    @Override
    public String getColumnPrefix() {
        return "`";
    }

    @Override
    public String getColumnSuffix() {
        return "`";
    }
}
