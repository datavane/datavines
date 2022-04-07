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

import io.datavines.server.coordinator.repository.entity.Job;

public interface JobService {

    /**
     * 返回主键字段id值
     * @param job
     * @return
     */
    long insert(Job job);

    /**
     * updateById
     * @param job
     * @return
     */
    int update(Job job);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Job getById(long id);

    List<Job> listByDataSourceId(Long dataSourceId);

    /**
     * delete by id
     * @param id id
     * @return int
     */
    int deleteById(long id);
}
