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

import io.datavines.common.utils.ConnectionUtils;
import io.datavines.registry.api.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

@Slf4j
public class MysqlRegistry implements Registry {

    private MysqlMutex mysqlMutex;

    private MysqlServerStateManager mysqlServerStateManager;

    @Override
    public void init(Properties properties) throws Exception {

        Connection connection = ConnectionUtils.getConnection(properties);

        if (connection == null){
            throw new Exception("can not create connection");
        }

        try {
            mysqlMutex = new MysqlMutex(connection, properties);
            mysqlServerStateManager = new MysqlServerStateManager(connection, properties);
        } catch (SQLException exception) {
            log.error("init mysql mutex error: " + exception.getLocalizedMessage());
        }
    }

    @Override
    public boolean acquire(String key, long timeout){
        try {
            return mysqlMutex.acquire(key, timeout);
        } catch (Exception e) {
            log.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public boolean release(String key){
        try {
            return mysqlMutex.release(key);
        } catch (Exception e) {
            log.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public void subscribe(String key, SubscribeListener subscribeListener) {
        try {
            mysqlServerStateManager.registry(subscribeListener);
        } catch (Exception e){
            log.warn("subscribe error: ", e);
        }

    }

    @Override
    public void unSubscribe(String key) {
        try {
            mysqlServerStateManager.unRegistry();
        } catch (Exception e){
            log.warn("unSubscribe error: ", e);
        }
    }

    @Override
    public void addConnectionListener(ConnectionListener connectionListener) {

    }

    @Override
    public List<ServerInfo> getActiveServerList() {
        return mysqlServerStateManager.getActiveServerList();
    }

    @Override
    public void close() throws SQLException {
        mysqlMutex.close();
        mysqlServerStateManager.close();
    }
}
