package io.datavines.server.utils;

import com.zaxxer.hikari.HikariDataSource;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.utils.JdbcUrlParser;

public class DefaultDataSourceInfoUtils {

    public static ConnectionInfo getDefaultConnectionInfo(){
        javax.sql.DataSource defaultDataSource =
                SpringApplicationContext.getBean(javax.sql.DataSource.class);
        HikariDataSource hikariDataSource = (HikariDataSource)defaultDataSource;

        ConnectionInfo connectionInfo = JdbcUrlParser.getConnectionInfo(hikariDataSource.getJdbcUrl(),
                hikariDataSource.getUsername(),hikariDataSource.getPassword());

        if(connectionInfo != null) {
            connectionInfo.setDriverName(hikariDataSource.getDriverClassName());
        }

        return connectionInfo;
    }
}
