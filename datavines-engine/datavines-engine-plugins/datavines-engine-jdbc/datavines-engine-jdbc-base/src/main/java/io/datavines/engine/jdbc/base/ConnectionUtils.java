package io.datavines.engine.jdbc.base;

import io.datavines.common.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);

    public static Connection getConnection(String driver, Config config) {
        String url = config.getString("url");
        String username = config.getString("user");
        String password = config.getString("password");
        try {
            Class.forName(driver);
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
}
