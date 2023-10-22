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
package io.datavines.engine.local.api.entity;

import com.alibaba.druid.pool.DruidPooledConnection;
import io.datavines.common.config.Config;
import io.datavines.common.utils.ConnectionUtils;
import io.datavines.engine.local.api.utils.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionHolder {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionHolder.class);
    private Connection connection;

    private final Config config;

    public ConnectionHolder(Config config){
        this.config = config;
    }

    public ConnectionHolder(Connection connection, Config config) {
        this.connection = connection;
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(10)) {
            connection = ConnectionUtils.getConnection(config, logger);
        }
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
