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

import io.datavines.common.dto.datasource.ExecuteRequest;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.param.TestConnectionRequestParam;
import io.datavines.common.dto.datasource.DataSourceCreate;
import io.datavines.common.dto.datasource.DataSourceUpdate;
import io.datavines.server.coordinator.repository.entity.DataSource;
import io.datavines.server.exception.DataVinesServerException;

import java.util.List;

public interface DataSourceService {

    boolean testConnect(TestConnectionRequestParam connectionParam);

    long insert(DataSourceCreate dataSource);

    int update(DataSourceUpdate dataSource) throws DataVinesException;

    DataSource getById(long id);

    int delete(long id);

    List<DataSource> listByWorkSpaceId(long workspaceId);

    Object getDatabaseList(Long id) throws DataVinesServerException;

    Object getTableList(Long id, String database) throws DataVinesServerException;

    Object getColumnList(Long id, String database, String table) throws DataVinesServerException;

    Object executeScript(ExecuteRequest request) throws DataVinesServerException;

    String getConfigJson(String type);
}
