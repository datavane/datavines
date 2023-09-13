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
package io.datavines.server.registry;

import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.common.utils.NetUtils;
import io.datavines.common.utils.Stopper;
import io.datavines.common.utils.ThreadUtils;
import io.datavines.registry.api.Registry;
import io.datavines.registry.api.ServerInfo;
import io.datavines.registry.api.SubscribeListener;
import io.datavines.server.catalog.metadata.CatalogMetaDataFetchTaskFailover;
import io.datavines.server.dqc.coordinator.failover.JobExecutionFailover;
import io.datavines.server.repository.entity.Config;
import io.datavines.server.repository.service.ConfigService;
import io.datavines.server.utils.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

@Slf4j
public class Register {

    private final Registry registry;

    private final JobExecutionFailover jobExecutionFailover;

    private final CatalogMetaDataFetchTaskFailover catalogMetaDataFetchTaskFailover;

    private final ConfigService configService;

    private volatile int currentSlot = 0;

    private volatile int totalSlot = 0;

    private static final Integer QUEUE_MAX_SIZE = 20;

    private final String serverKey = NetUtils.getHost() + ":" + CommonPropertyUtils.getString(CommonPropertyUtils.SERVER_PORT);

    private final PriorityBlockingQueue<ServerInfo> queue = new PriorityBlockingQueue<>(QUEUE_MAX_SIZE, new ServerComparator());

    private final HashMap<String, Integer> hostIndexMap = new HashMap<>();

    private final String FAILOVER_KEY =
            CommonPropertyUtils.getString(CommonPropertyUtils.FAILOVER_KEY, CommonPropertyUtils.FAILOVER_KEY_DEFAULT);

    public Register(Registry registry, JobExecutionFailover jobExecutionFailover, CatalogMetaDataFetchTaskFailover catalogMetaDataFetchTaskFailover) {
        this.registry = registry;
        this.jobExecutionFailover = jobExecutionFailover;
        this.catalogMetaDataFetchTaskFailover = catalogMetaDataFetchTaskFailover;
        this.configService = SpringApplicationContext.getBean(ConfigService.class);
        updateCommonProperties();
    }

    private void updateCommonProperties() {
        List<Config> configList = configService.listConfig();
        if (CollectionUtils.isNotEmpty(configList)) {
            configList.forEach(config -> {
                CommonPropertyUtils.setValue(config.getVarKey(), config.getVarValue());
            });
        }
        log.info("common properties: {}", CommonPropertyUtils.getProperties());
    }

    public void start() {
        ThreadUtils.sleep(3000);
        registry.subscribe("", event -> {
            log.info("receive event: {}", event);
            switch (event.type()) {
                case ADD:
                    // TODO
                    break;
                case REMOVE:
                    try {
                        blockUtilAcquireLock(FAILOVER_KEY);
                        jobExecutionFailover.handleJobExecutionFailover(event.key());
                        catalogMetaDataFetchTaskFailover.handleMetaDataFetchTaskFailover(event.key());
                    } finally {
                        registry.release(FAILOVER_KEY);
                    }
                    break;
                default:
                    break;
            }

            updateServerListInfo();
        });

        try {
            blockUtilAcquireLock(FAILOVER_KEY);
            //Query whether the current server has any tasks that need fault tolerance according to the ip:port
            String host = NetUtils.getAddr(CommonPropertyUtils.getInt(
                    CommonPropertyUtils.SERVER_PORT, CommonPropertyUtils.SERVER_PORT_DEFAULT));
            jobExecutionFailover.handleJobExecutionFailover(host);
            catalogMetaDataFetchTaskFailover.handleMetaDataFetchTaskFailover(host);

            List<ServerInfo> activeServerInfoList = registry.getActiveServerList();
            //Get the current active server, and then get all running tasks of the server other than the active server list
            List<String> activeServerList = activeServerInfoList
                    .stream()
                    .map(ServerInfo::getAddr)
                    .collect(Collectors.toList());

            jobExecutionFailover.handleJobExecutionFailover(activeServerList);
            catalogMetaDataFetchTaskFailover.handleMetaDataFetchTaskFailover(activeServerList);

        } finally {
            registry.release(FAILOVER_KEY);
        }

        updateServerListInfo();
    }

    public void blockUtilAcquireLock(String key) {
        while (Stopper.isRunning() && !registry.acquire(key, 10)) {
                ThreadUtils.sleep(1000);
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

    public int getSlot() {
        return currentSlot;
    }

    public int getTotalSlot() {
        return totalSlot;
    }

    public void updateServerListInfo() {
        log.info("active server list:{}", registry.getActiveServerList());
        queue.clear();
        queue.addAll(registry.getActiveServerList());
        hostIndexMap.clear();
        Iterator<ServerInfo> iterator = queue.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            ServerInfo server = iterator.next();
            String addr = NetUtils.getAddr(server.getHost(), server.getServerPort());
            hostIndexMap.put(addr, index++);
        }

        if (!hostIndexMap.containsKey(serverKey)) {
            currentSlot = -1;
        } else {
            currentSlot = hostIndexMap.get(serverKey);
            if (currentSlot >= 0) {
                totalSlot = registry.getActiveServerList().size();
            } else {
                log.warn("Current master is not in active master list");
            }
        }

        log.info("Current slot is " + currentSlot + " total slot is " + totalSlot);
    }

    private static class ServerComparator implements Comparator<ServerInfo> {

        @Override
        public int compare(ServerInfo o1, ServerInfo o2) {
            return o2.getCreateTime().compareTo(o1.getCreateTime());
        }
    }
}
