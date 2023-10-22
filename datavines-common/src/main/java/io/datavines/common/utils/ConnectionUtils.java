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
package io.datavines.common.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import io.datavines.common.config.Config;
import io.datavines.common.datasource.jdbc.JdbcDataSourceManager;
import io.datavines.common.exception.DataVinesException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class ConnectionUtils {

    public static Connection getConnection(Config config) throws DataVinesException {
        Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);
        return getConnection(config, logger);
    }

    public static Connection getConnection(Map<String,Object> configMap) throws DataVinesException {
        Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);
        return getConnection(configMap, logger);
    }

    public static Connection getConnection(Config config, Logger logger) throws DataVinesException {
        return getConnection(config.configMap(), logger);
    }

    public static Connection getConnection(Properties properties) {
        Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);
        return getConnection(properties, logger);
    }

    public static Connection getConnection(Map<String,Object> configMap, Logger logger) throws DataVinesException {
        try {
            DataSource dataSource = JdbcDataSourceManager.getInstance().getDataSource(configMap);
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

    public static Connection getConnection(Properties properties,Logger logger) throws DataVinesException {
        try {
            DataSource dataSource = JdbcDataSourceManager.getInstance().getDataSource(properties);
            if (dataSource != null) {
                Connection connection = dataSource.getConnection();
                logger.info("get connection success : {}", properties.get("url") + "[username=" + properties.get("user") + "]");
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
}
