package io.datavines.engine.jdbc.mysql.source;

import io.datavines.engine.jdbc.base.BaseJdbcSource;

/**
 * MysqlSource
 */
public class MysqlSource extends BaseJdbcSource {

    @Override
    protected String getDriver() {
        return "com.mysql.jdbc.Driver";
    }
}
