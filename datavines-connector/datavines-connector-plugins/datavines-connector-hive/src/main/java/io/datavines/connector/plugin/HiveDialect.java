package io.datavines.connector.plugin;

public class HiveDialect extends JdbcDialect {

    @Override
    public String getDriver() {
        return "org.apache.hive.jdbc.HiveDriver";
    }
}
