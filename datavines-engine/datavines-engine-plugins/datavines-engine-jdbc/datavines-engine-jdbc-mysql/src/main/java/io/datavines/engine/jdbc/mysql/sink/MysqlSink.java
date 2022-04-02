package io.datavines.engine.jdbc.mysql.sink;

import io.datavines.engine.jdbc.base.BaseJdbcSink;

/**
 * MysqlSink
 */
public class MysqlSink extends BaseJdbcSink {

    @Override
    protected String getDriver() {
        return "com.mysql.jdbc.Driver";
    }
}