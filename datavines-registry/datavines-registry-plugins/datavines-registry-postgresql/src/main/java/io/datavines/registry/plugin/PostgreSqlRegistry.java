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
package io.datavines.registry.plugin;

import io.datavines.registry.api.ConnectionListener;
import io.datavines.registry.api.Registry;
import io.datavines.registry.api.ServerInfo;
import io.datavines.registry.api.SubscribeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class PostgreSqlRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlRegistry.class);

    private PostgreSqlMutex postgreSqlMutex;

    private PostgreSqlServerStateManager postgreSqlServerStateManager;

    @Override
    public void init(Properties properties) throws Exception {

        Connection connection = ConnectionUtils.getConnection(properties);

        if (connection == null){
            throw new Exception("can not create connection");
        }

        try {
            postgreSqlMutex = new PostgreSqlMutex(connection, properties);
            postgreSqlServerStateManager = new PostgreSqlServerStateManager(connection, properties);
        } catch (SQLException exception) {
            logger.error("init mysql mutex error: " + exception.getLocalizedMessage());
        }
    }

    @Override
    public boolean acquire(String key, long timeout){
        try {
            return postgreSqlMutex.acquire(key, timeout);
        } catch (Exception e) {
            logger.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public boolean release(String key){
        try {
            return postgreSqlMutex.release();
        } catch (Exception e) {
            logger.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public void subscribe(String key, SubscribeListener subscribeListener) {
        try {
            postgreSqlServerStateManager.registry(subscribeListener);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void unSubscribe(String key) {
        try {
            postgreSqlServerStateManager.unRegistry();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addConnectionListener(ConnectionListener connectionListener) {

    }

    @Override
    public List<ServerInfo> getActiveServerList() {
        return postgreSqlServerStateManager.getActiveServerList();
    }

    @Override
    public void close() throws SQLException {
        postgreSqlMutex.close();
        postgreSqlServerStateManager.close();
    }
}
