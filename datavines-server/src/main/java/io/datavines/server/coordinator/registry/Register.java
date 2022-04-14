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

package io.datavines.server.coordinator.registry;

import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.common.utils.NetUtils;
import io.datavines.common.utils.Stopper;
import io.datavines.registry.api.Event;
import io.datavines.registry.api.Registry;
import io.datavines.registry.api.ServerInfo;
import io.datavines.registry.api.SubscribeListener;
import io.datavines.server.coordinator.server.failover.TaskFailover;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Register {

    private final Registry registry;

    private final TaskFailover taskFailover;

    private final String FAILOVER_KEY =
            CommonPropertyUtils.getString(CommonPropertyUtils.FAILOVER_KEY, CommonPropertyUtils.FAILOVER_KEY_DEFAULT);

    public Register(Registry registry, TaskFailover taskFailover) {
        this.registry = registry;
        this.taskFailover = taskFailover;
    }

    public void start() {
        registry.subscribe("", event -> {
            if (Event.Type.REMOVE == event.type()) {
                try {
                    blockUtilAcquireLock(FAILOVER_KEY);
                    taskFailover.handleTaskFailover(event.key());
                } finally {
                    registry.release(FAILOVER_KEY);
                }
            }
        });

        try {
            blockUtilAcquireLock(FAILOVER_KEY);
            //Query whether the current server has any tasks that need fault tolerance according to the ip:port
            String host = NetUtils.getAddr(CommonPropertyUtils.getInt(
                    CommonPropertyUtils.SERVER_PORT, CommonPropertyUtils.SERVER_PORT_DEFAULT));
            taskFailover.handleTaskFailover(host);

            List<ServerInfo> activeServerList = registry.getActiveServerList();
            //Get the current active server, and then get all running tasks of the server other than the active server list
            taskFailover.handleTaskFailover(
                    activeServerList
                            .stream()
                            .map(ServerInfo::toString)
                            .collect(Collectors.toList()));
        } finally {
            registry.release(FAILOVER_KEY);
        }
    }

    public void blockUtilAcquireLock(String key) {
        while (Stopper.isRunning()
                &&!registry.acquire(key, 10)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean acquire(String key, long timeout){
        return registry.acquire(key, timeout);
    }

    public void release(String key) {
        registry.release(key);
    }

    public void subscribe(String key, SubscribeListener listener){
        registry.subscribe(key, listener);
    }

    public void unSubscribe(String key){
       registry.unSubscribe(key);
    }

    public void close() throws SQLException {
        registry.close();
    }
}
