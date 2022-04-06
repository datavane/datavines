package io.datavines.registry.plugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class PostgreSqlMutex {

    private Connection connection;
    private final Properties properties;
    private Statement statement;
    private String lockKey;

    public PostgreSqlMutex(Connection connection, Properties properties) throws SQLException {
        this.connection = connection;
        this.properties = properties;
        statement = connection.createStatement();
    }

    public boolean acquire(String key, long time) throws SQLException {
        lockKey = "'" + key + "'";
        String sql = String.format("select pg_try_advisory_lock(%s,%d)", lockKey ,time);
        return executeSql(sql);
    }

    public boolean release() throws SQLException {
        String sql = String.format("select pg_advisory_unlock(%s)", lockKey);
        return executeSql(sql);
    }

    public boolean release(String key) throws SQLException {
        String sql = String.format("select pg_advisory_unlock(%s)", key);
        return executeSql(sql);
    }

    private boolean executeSql(String sql) throws SQLException {

        if(connection == null || connection.isClosed()) {
            connection = ConnectionUtils.getConnection(properties);
        }

        if(statement == null || statement.isClosed()){
            statement = connection.createStatement();
        }

        ResultSet resultSet = statement.executeQuery(sql);

        if (resultSet == null) {
            return false;
        }

        if (resultSet.first()) {
            int result = resultSet.getInt(1);
            resultSet.close();
            return result >= 1;
        } else {
            resultSet.close();
            return false;
        }
    }

    public boolean isUsedLock() throws SQLException {
        String sql = String.format("select is_used_lock(%s)", lockKey);
        return executeSql(sql);
    }

    public boolean isFreeLock() throws SQLException {
        String sql = String.format("select is_free_lock(%s)", lockKey);
        return executeSql(sql);
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
