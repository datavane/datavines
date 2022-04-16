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

import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.common.zookeeper.ZooKeeperClient;
import io.datavines.common.zookeeper.ZooKeeperConfig;
import io.datavines.registry.api.ConnectionListener;
import io.datavines.registry.api.Registry;
import io.datavines.registry.api.ServerInfo;
import io.datavines.registry.api.SubscribeListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ZooKeeperRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRegistry.class);

    private InterProcessMutex mutex;

    private CuratorFramework client;

    @Override
    public void init(Properties properties) {
        ZooKeeperClient zooKeeperClient = null;
        try {
            ZooKeeperConfig zooKeeperConfig = new ZooKeeperConfig();
            zooKeeperConfig.setServerList(properties.getProperty(CommonPropertyUtils.REGISTRY_ZOOKEEPER_SERVER_LIST,
                    CommonPropertyUtils.REGISTRY_ZOOKEEPER_SERVER_LIST_DEFAULT));
            zooKeeperClient = ZooKeeperClient.getInstance().buildClient(zooKeeperConfig);
            client = zooKeeperClient.getClient();
        } catch (Exception exception) {
            logger.error("build zookeeper client error: {} ", exception);
        }
    }

    @Override
    public boolean acquire(String key, long timeout){

        if (client == null) {
            return false;
        }

        if (mutex == null) {
            mutex = new InterProcessMutex(client,key);
        }

        try {
            return mutex.acquire(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public boolean release(String key){

        if (client == null) {
            return false;
        }

        try {
            if (mutex != null && mutex.isAcquiredInThisProcess()) {
                mutex.release();
                return true;
            }
            return true;
        } catch (Exception e) {
            logger.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public void subscribe(String key, SubscribeListener listener) {

    }

    @Override
    public void unSubscribe(String key) {

    }

    @Override
    public void addConnectionListener(ConnectionListener connectionListener) {

    }

    @Override
    public List<ServerInfo> getActiveServerList() {
        return null;
    }

    @Override
    public void close() throws SQLException {

    }
}
