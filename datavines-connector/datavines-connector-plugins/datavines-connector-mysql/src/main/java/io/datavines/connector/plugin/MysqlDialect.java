package io.datavines.connector.plugin;

public class MysqlDialect extends JdbcDialect{

    @Override
    public String getDriver() {
        return "com.mysql.jdbc.Driver";
    }
}
