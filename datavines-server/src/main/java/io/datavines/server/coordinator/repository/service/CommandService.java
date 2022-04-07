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

import io.datavines.server.coordinator.repository.entity.Command;

public interface CommandService  {

    /**
     * 返回主键字段id值
     * @param command
     * @return
     */
    long insert(Command command);

    /**
     * updateById
     * @param command
     * @return
     */
    int update(Command command);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Command getById(long id);

    /**
     * SELECT BY ID
     * @return
     */
    Command getOne();

    /**
     * delete by id
     * @param id id
     * @return int
     */
    int deleteById(long id);
}
