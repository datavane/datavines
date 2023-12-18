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
import io.datavines.common.utils.Md5Utils;
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.DataSourceClient;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static io.datavines.common.ConfigConstants.*;

public class PrestoDataSourceClient implements DataSourceClient {

    private final ConcurrentHashMap<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    @Override
    public DataSource getDataSource(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        return getDataSourceInternal(baseJdbcDataSourceInfo);
    }

    @Override
    public DataSource getDataSource(Map<String, Object> configMap) throws SQLException {
        return getDataSourceInternal(configMap);
    }

    @Override
    public DataSource getDataSource(Properties properties) throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        return getDataSource(baseJdbcDataSourceInfo).getConnection();
    }

    @Override
    public Connection getConnection(Map<String, Object> configMap) throws SQLException {
        return getDataSource(configMap).getConnection();
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

    public DataSource getDataSourceInternal(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        if (baseJdbcDataSourceInfo == null) {
            return null;
        }

        DataSource dataSource = dataSourceMap.get(baseJdbcDataSourceInfo.getUniqueKey());

        if (dataSource == null) {
            BasicDataSource basicDataSource = new BasicDataSource();

            basicDataSource.setDriverClassName(baseJdbcDataSourceInfo.getDriverClass());
            basicDataSource.addConnectionProperty(USER, baseJdbcDataSourceInfo.getUser());
            if (!StringUtils.isEmptyOrNullStr(baseJdbcDataSourceInfo.getPassword())) {
                basicDataSource.addConnectionProperty(PASSWORD, baseJdbcDataSourceInfo.getPassword());
            }

            String[] url2Array = baseJdbcDataSourceInfo.getJdbcUrl().split("\\?");
            basicDataSource.setUrl(url2Array[0]);
            if (url2Array.length > 1) {
                String[] keyArray =  url2Array[1].split("&");
                for (String properties : keyArray) {
                    String[] values= properties.split("=");
                    basicDataSource.addConnectionProperty(values[0], values[1]);
                }
            }

            dataSourceMap.put(baseJdbcDataSourceInfo.getUniqueKey(), basicDataSource);
            return basicDataSource;
        }

        return dataSource;
    }

    public DataSource getDataSourceInternal(Map<String,Object> configMap) throws SQLException {
        if (configMap == null) {
            return null;
        }

        DataSource dataSource = dataSourceMap.get(getUniqueKey(configMap));

        if (dataSource == null) {
            String driver = String.valueOf(configMap.get(DRIVER));
            String url = String.valueOf(configMap.get(URL));
            String username = String.valueOf(configMap.get(USER));
            String password = String.valueOf(configMap.get(PASSWORD));

            BasicDataSource basicDataSource = new BasicDataSource();

            basicDataSource.setDriverClassName(driver);
            basicDataSource.addConnectionProperty(USER, username);
            if (!StringUtils.isEmptyOrNullStr(password)) {
                basicDataSource.addConnectionProperty(PASSWORD, password);
            }

            String[] url2Array = url.split("\\?");
            basicDataSource.setUrl(url2Array[0]);
            if (url2Array.length > 1) {
                String[] keyArray =  url2Array[1].split("&");
                for (String properties : keyArray) {
                    String[] values= properties.split("=");
                    basicDataSource.addConnectionProperty(values[0], values[1]);
                }
            }

            dataSourceMap.put(getUniqueKey(configMap), basicDataSource);
            return basicDataSource;
        }

        return dataSource;
    }

    private String getUniqueKey(Map<String,Object> configMap) {
        String url = String.valueOf(configMap.get(URL));
        String username = String.valueOf(configMap.get(USER));
        String password = String.valueOf(configMap.get(PASSWORD));
        return Md5Utils.getMd5(String.format("%s@@%s@@%s",url,username,password),false);
    }

}
