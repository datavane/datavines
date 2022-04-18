package io.datavines.connector.plugin.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataSourceManager
 */
@Slf4j
public class DataSourceManager {

    private final ConcurrentHashMap<String,DataSource> dataSourceMap = new ConcurrentHashMap<>();

    private static final class Singleton {
        private static final DataSourceManager INSTANCE = new DataSourceManager();
    }

    public static DataSourceManager getInstance() {
        return Singleton.INSTANCE;
    }

    public Connection getConnection(BaseDataSourceInfo baseDataSourceInfo) throws SQLException {
        return getDataSource(baseDataSourceInfo).getConnection();
    }

    public JdbcTemplate getJdbcTemplate(BaseDataSourceInfo baseDataSourceInfo) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource(baseDataSourceInfo));
        jdbcTemplate.setFetchSize(500);
        return jdbcTemplate;
    }

    public DataSource getDataSource(BaseDataSourceInfo baseDataSourceInfo) {
        if (baseDataSourceInfo == null) {
            return null;
        }

        DataSource dataSource = dataSourceMap.get(baseDataSourceInfo.getUniqueKey());

        if (dataSource == null) {
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setUrl(baseDataSourceInfo.getJdbcUrl());
            druidDataSource.setUsername(baseDataSourceInfo.getUser());
            druidDataSource.setPassword(baseDataSourceInfo.getPassword());
            druidDataSource.setDriverClassName(baseDataSourceInfo.getDriverClass());
            dataSourceMap.put(baseDataSourceInfo.getUniqueKey(), druidDataSource);
            return druidDataSource;
        }

        return dataSource;
    }
}
