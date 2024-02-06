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
package io.datavines.common.datasource.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.datavines.common.utils.Md5Utils;
import io.datavines.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import static io.datavines.common.ConfigConstants.*;

@Slf4j
public class JdbcDataSourceManager {

    private final ConcurrentHashMap<String, HikariDataSource> dataSourceMap = new ConcurrentHashMap<>();

    private static final class Singleton {
        private static final JdbcDataSourceManager INSTANCE = new JdbcDataSourceManager();
    }

    public static JdbcDataSourceManager getInstance() {
        return Singleton.INSTANCE;
    }

    public DataSource getDataSource(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        if (baseJdbcDataSourceInfo == null) {
            return null;
        }

        HikariDataSource dataSource = dataSourceMap.get(baseJdbcDataSourceInfo.getUniqueKey());
        if (dataSource == null) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(baseJdbcDataSourceInfo.getJdbcUrl());
            hikariConfig.setUsername(baseJdbcDataSourceInfo.getUser());
            hikariConfig.setPassword(StringUtils.isEmptyOrNullStr(baseJdbcDataSourceInfo.getPassword()) ? null : baseJdbcDataSourceInfo.getPassword());
            hikariConfig.setDriverClassName(baseJdbcDataSourceInfo.getDriverClass());
            hikariConfig.setMaximumPoolSize(10);
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            dataSourceMap.put(baseJdbcDataSourceInfo.getUniqueKey(), hikariDataSource);
            return hikariDataSource;
        }

        return dataSource;
    }

    public DataSource getDataSource(Map<String,Object> configMap) throws SQLException {
        String uniqueKey = getUniqueKey(configMap);
        // 暂时用 HikariDataSource 替代 DruidDataSource
        HikariDataSource dataSource = dataSourceMap.get(getUniqueKey(configMap));

        if (dataSource == null) {
            String driver = String.valueOf(configMap.get(DRIVER));
            String url = String.valueOf(configMap.get(URL));
            String username = String.valueOf(configMap.get(USER));
            String password = String.valueOf(configMap.get(PASSWORD));
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(StringUtils.isEmptyOrNullStr(password) ? null : password);
            hikariConfig.setDriverClassName(driver);
            hikariConfig.setMaximumPoolSize(10);
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            dataSourceMap.put(uniqueKey, hikariDataSource);
            return hikariDataSource;
        }

        return dataSource;
    }

    public DataSource getDataSource(Properties properties) throws SQLException {
        Map<String,Object> configMap = new HashMap<>();
        configMap.put(URL, properties.getProperty("url"));
        configMap.put(DRIVER, properties.getProperty("driver"));
        configMap.put(USER, properties.getProperty("username"));
        configMap.put(PASSWORD, properties.getProperty("password"));

        String uniqueKey = getUniqueKey(configMap);
        HikariDataSource dataSource = dataSourceMap.get(getUniqueKey(configMap));

        if (dataSource == null) {
            String driver = String.valueOf(configMap.get(DRIVER));
            String url = String.valueOf(configMap.get(URL));
            String username = String.valueOf(configMap.get(USER));
            String password = String.valueOf(configMap.get(PASSWORD));
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(StringUtils.isEmptyOrNullStr(password) ? null : password);
            hikariConfig.setDriverClassName(driver);
            hikariConfig.setMaximumPoolSize(10);
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            dataSourceMap.put(uniqueKey, hikariDataSource);
            return hikariDataSource;
        }

        return dataSource;
    }

    private String getUniqueKey(Map<String,Object> configMap) {
        String url = String.valueOf(configMap.get(URL));
        String username = String.valueOf(configMap.get(USER));
        String password = String.valueOf(configMap.get(PASSWORD));
        return Md5Utils.getMd5(String.format("%s@@%s@@%s",url,username,password),false);
    }

    public void close() {
        dataSourceMap.forEach((key, value) -> value.close());
    }
}
