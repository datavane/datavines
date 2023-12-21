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
package io.datavines.connector.plugin;

import io.datavines.common.datasource.jdbc.BaseJdbcDataSourceInfo;
import io.datavines.common.datasource.jdbc.JdbcDataSourceManager;
import io.datavines.common.exception.DataVinesException;
import io.datavines.connector.api.DataSourceClient;
import org.slf4j.Logger;
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
    public Connection getConnection(Map<String,Object> configMap, Logger logger) throws DataVinesException {
        try {
            DataSource dataSource = getDataSource(configMap);
            if (dataSource != null) {
                Connection connection = dataSource.getConnection();
                logger.info("get connection success : {}",  configMap.get("url") + "[username=" + configMap.get("user") + "]");
                return connection;
            } else {
                logger.error("get datasource error");
                throw new DataVinesException("can not get datasource");
            }
        } catch (SQLException exception) {
            logger.error("get connection error :", exception);
            throw new DataVinesException(exception);
        }
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
