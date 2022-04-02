package io.datavines.engine.spark.mysql.sink;

import io.datavines.engine.spark.jdbc.sink.BaseJdbcSink;

/**
 * MysqlSink
 */
public class MysqlSink extends BaseJdbcSink {

    @Override
    protected String getDriver() {
        return "com.mysql.jdbc.Driver";
    }
}