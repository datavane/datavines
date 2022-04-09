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

package io.datavines.server.coordinator.repository.service;

import java.util.List;

import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.api.dto.task.SubmitTask;
import io.datavines.server.coordinator.repository.entity.Task;

public interface TaskService {

    /**
     * 返回主键字段id值
     * @param task
     * @return
     */
    long insert(Task task);

    /**
     * updateById
     * @param task
     * @return
     */
    int update(Task task);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Task getById(long id);

    /**
     * 根据dataSourceId获取task列表
     * @param dataSourceId
     * @return
     */
    List<Task> listByDataSourceId(long dataSourceId);

    Long submitTask(SubmitTask submitTask) throws DataVinesException;

    Long killTask(Long taskId);

    List<Task> listNeedFailover(String host);

    List<Task> listTaskNotInServerList(List<String> hostList);

}
