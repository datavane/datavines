package io.datavines.connector.plugin;

import io.datavines.common.datasource.jdbc.BaseJdbcDataSourceInfo;
import io.datavines.common.datasource.jdbc.JdbcDataSourceManager;
import io.datavines.connector.api.DataSourceClient;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class JdbcDataSourceClient implements DataSourceClient {

    @Override
    public DataSource getDataSource(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(baseJdbcDataSourceInfo);
    }

    @Override
    public DataSource getDataSource(Map<String, Object> configMap) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(configMap);
    }

    @Override
    public DataSource getDataSource(Properties properties) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(properties);
    }

    @Override
    public Connection getConnection(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(baseJdbcDataSourceInfo).getConnection();
    }

    @Override
    public Connection getConnection(Map<String, Object> configMap) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(configMap).getConnection();
    }

    @Override
    public Connection getConnection(Properties properties) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(properties).getConnection();
    }

    @Override
    public JdbcTemplate getJdbcTemplate(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(JdbcDataSourceManager.getInstance().getDataSource(baseJdbcDataSourceInfo));
        jdbcTemplate.setFetchSize(500);
        return jdbcTemplate;
    }
}
