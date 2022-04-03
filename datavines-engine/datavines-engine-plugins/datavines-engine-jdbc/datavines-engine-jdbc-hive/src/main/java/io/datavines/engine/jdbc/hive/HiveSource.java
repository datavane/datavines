package io.datavines.engine.jdbc.hive;

import io.datavines.engine.jdbc.base.BaseJdbcSource;

public class HiveSource extends BaseJdbcSource {

    @Override
    protected String getDriver() {
        return "org.apache.hive.jdbc.HiveDriver";
    }
}
