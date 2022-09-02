/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.common.jdbc.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import io.datavines.common.utils.StringUtils;
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
            druidDataSource.setPassword(StringUtils.isEmpty(baseDataSourceInfo.getPassword()) ? null : baseDataSourceInfo.getPassword());
            druidDataSource.setDriverClassName(baseDataSourceInfo.getDriverClass());
            druidDataSource.setBreakAfterAcquireFailure(true);
            dataSourceMap.put(baseDataSourceInfo.getUniqueKey(), druidDataSource);
            return druidDataSource;
        }

        return dataSource;
    }
}
