package io.datavines.connector.api;

import io.datavines.common.datasource.jdbc.BaseJdbcDataSourceInfo;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public interface DataSourceClient {

    DataSource getDataSource(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException;

    DataSource getDataSource(Map<String,Object> configMap) throws SQLException;

    DataSource getDataSource(Properties properties) throws SQLException;

    Connection getConnection(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException;

    Connection getConnection(Map<String,Object> configMap) throws SQLException;

    Connection getConnection(Properties properties) throws SQLException;

    JdbcTemplate getJdbcTemplate(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException;
}
