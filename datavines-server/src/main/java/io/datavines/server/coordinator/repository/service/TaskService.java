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

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.datavines.server.coordinator.api.dto.bo.task.SubmitTask;
import io.datavines.server.coordinator.api.dto.vo.TaskVO;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.core.exception.DataVinesServerException;

public interface TaskService {

    long create(Task task);

    int update(Task task);

    Task getById(long id);

    List<Task> listByJobId(long jobId);

    IPage<TaskVO> getTaskPage(String searchVal, Long jobId, Integer pageNumber, Integer pageSize);

    Long submitTask(SubmitTask submitTask) throws DataVinesServerException;

    Long executeTask(Task task) throws DataVinesServerException;

    Long killTask(Long taskId);

    List<Task> listNeedFailover(String host);

    List<Task> listTaskNotInServerList(List<String> hostList);

    Object readErrorDataPage(Long taskId, Integer pageNumber, Integer pageSize);
}
