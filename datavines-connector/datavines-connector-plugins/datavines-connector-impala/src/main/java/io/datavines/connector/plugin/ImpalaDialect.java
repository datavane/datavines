package io.datavines.connector.plugin;

public class ImpalaDialect extends JdbcDialect {

    @Override
    public String getDriver() {
        return "org.apache.hive.jdbc.HiveDriver";
    }
}
