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

import io.datavines.server.catalog.task.CatalogMetaDataFetchTaskImpl;
import io.datavines.server.catalog.task.CatalogTaskContext;
import io.datavines.server.catalog.task.CatalogTaskResponse;
import io.datavines.server.catalog.task.CatalogTaskResponseQueue;
import io.datavines.server.utils.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CatalogTaskRunner implements Runnable {

    private final CatalogTaskContext taskContext;

    private final CatalogTaskResponseQueue responseQueue =
            SpringApplicationContext.getBean(CatalogTaskResponseQueue.class);

    public CatalogTaskRunner(CatalogTaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void run() {
        CatalogMetaDataFetchTaskImpl fetchTask = new CatalogMetaDataFetchTaskImpl(taskContext.getMetaDataFetchRequest());
        try {
            fetchTask.execute();
            log.info("fetch metadata finished");
            responseQueue.add(new CatalogTaskResponse(taskContext.getCatalogTaskId(), 1));
        } catch (Exception e) {
            log.error("fetch metadata error: ", e);
            responseQueue.add(new CatalogTaskResponse(taskContext.getCatalogTaskId(), 2));
        }
    }
}
