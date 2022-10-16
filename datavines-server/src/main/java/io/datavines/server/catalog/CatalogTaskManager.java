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
package io.datavines.server.catalog;

import io.datavines.common.utils.*;
import io.datavines.server.catalog.enums.FetchType;
import io.datavines.server.catalog.task.CatalogTaskContext;
import io.datavines.server.catalog.task.CatalogTaskResponse;
import io.datavines.server.catalog.task.CatalogTaskResponseQueue;
import io.datavines.server.catalog.task.MetaDataFetchRequest;
import io.datavines.server.repository.entity.DataSource;
import io.datavines.server.repository.entity.catalog.CatalogTask;
import io.datavines.server.repository.service.CatalogTaskService;
import io.datavines.server.repository.service.impl.JobExternalService;
import io.datavines.server.utils.NamedThreadFactory;
import io.datavines.server.utils.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class CatalogTaskManager {

    private final LinkedBlockingQueue<CatalogTaskContext> taskQueue = new LinkedBlockingQueue<>();

    private final CatalogTaskResponseQueue responseQueue =
            SpringApplicationContext.getBean(CatalogTaskResponseQueue.class);

    private final CatalogTaskService catalogTaskService =
            SpringApplicationContext.getBean(CatalogTaskService.class);

    private final JobExternalService jobExternalService =
            SpringApplicationContext.getBean(JobExternalService.class);

    private final ExecutorService taskExecuteService;

    public CatalogTaskManager () {
        this.taskExecuteService = Executors.newFixedThreadPool(
                CommonPropertyUtils.getInt(CommonPropertyUtils.EXEC_THREADS,CommonPropertyUtils.EXEC_THREADS_DEFAULT),
                new NamedThreadFactory("CatalogTask-Execute-Thread"));
    }

    public void start() {
        new TaskExecutor().start();

        new TaskResponseOperator().start();
    }

    class TaskExecutor extends Thread {

        @Override
        public void run() {
            while(Stopper.isRunning()) {
                try {
                    CatalogTaskContext catalogTaskContext = taskQueue.take();
                    taskExecuteService.execute(new CatalogTaskRunner(catalogTaskContext));
                    ThreadUtils.sleep(1000);
                } catch(Exception e) {
                    log.error("dispatcher catalog task error",e);
                    ThreadUtils.sleep(2000);
                }
            }
        }
    }

    /**
     * operate task response
     */
    class TaskResponseOperator extends Thread {

        @Override
        public void run() {
            while (Stopper.isRunning()) {
                try {
                    CatalogTaskResponse taskResponse = responseQueue.take();
                    log.info("CatalogTaskResponse: " + JSONUtils.toJsonString(taskResponse));
                    CatalogTask catalogTask = catalogTaskService.getById(taskResponse.getCatalogTaskId());
                    if (catalogTask != null) {
                        catalogTask.setStatus(taskResponse.getStatus());
                        catalogTaskService.update(catalogTask);
                    }
                    ThreadUtils.sleep(1000);
                } catch(Exception e) {
                    log.info("operate catalog task response error {0}", e);
                }
            }
        }
    }

    public void putCatalogTask(CatalogTask catalogTask) throws InterruptedException {
        if (catalogTask == null) {
            return;
        }

        Long dataSourceId = catalogTask.getDataSourceId();
        DataSource dataSource = jobExternalService.getDataSourceService().getDataSourceById(dataSourceId);
        if (dataSource == null) {
            return;
        }

        MetaDataFetchRequest metaDataFetchRequest = new MetaDataFetchRequest();
        metaDataFetchRequest.setDataSource(dataSource);
        metaDataFetchRequest.setFetchType(FetchType.DATASOURCE);

        String parameter = catalogTask.getParameter();
        if (StringUtils.isNotEmpty(parameter)) {
            Map<String, String> parameterMap = JSONUtils.toMap(parameter);
            if (parameterMap != null) {
                String database = parameterMap.get("database");
                if (StringUtils.isNotEmpty(database)) {
                    metaDataFetchRequest.setDatabase(database);
                    metaDataFetchRequest.setFetchType(FetchType.DATABASE);
                }

                String table = parameterMap.get("table");
                if (StringUtils.isNotEmpty(table)) {
                    metaDataFetchRequest.setTable(table);
                    metaDataFetchRequest.setFetchType(FetchType.TABLE);
                }
            }
        }

        CatalogTaskContext catalogTaskContext = new CatalogTaskContext(metaDataFetchRequest, catalogTask.getId());
        taskQueue.put(catalogTaskContext);
    }

}
