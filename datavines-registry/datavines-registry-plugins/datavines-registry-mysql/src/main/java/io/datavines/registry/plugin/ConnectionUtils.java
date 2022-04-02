package io.datavines.registry.plugin;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);

    public static Connection getConnection(Properties properties) {
        String className = "com.mysql.jdbc.Driver";
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        try {
            Class.forName(className);
        } catch (ClassNotFoundException exception) {
            logger.error("load driver error: " + exception.getLocalizedMessage());
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException exception) {
            logger.error("get connection error: " + exception.getLocalizedMessage());

        }

        return connection;
    }

    public DataSource getDataSource(Properties properties) {
        if (properties == null) {
            return null;
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(properties.getProperty("url"));
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }
}
