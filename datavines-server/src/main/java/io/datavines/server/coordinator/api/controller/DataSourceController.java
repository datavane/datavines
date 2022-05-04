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

package io.datavines.server.coordinator.api.controller;

import io.datavines.common.exception.DataVinesException;
import io.datavines.common.param.TestConnectionRequestParam;
import io.datavines.server.DataVinesConstants;
import io.datavines.common.dto.datasource.DataSourceCreate;
import io.datavines.common.dto.datasource.DataSourceUpdate;
import io.datavines.server.coordinator.api.aop.RefreshToken;
import io.datavines.server.coordinator.api.entity.ResultMap;
import io.datavines.server.coordinator.repository.service.DataSourceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(value = "datasource", tags = "datasource", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/datasource", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @ApiOperation(value = "test connection")
    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object testConnection(@RequestBody TestConnectionRequestParam param)  {
        return dataSourceService.testConnect(param);
    }

    @ApiOperation(value = "create datasource")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createDataSource(@RequestBody DataSourceCreate dataSourceCreate)  {
        return dataSourceService.insert(dataSourceCreate);
    }

    @ApiOperation(value = "update datasource")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateDataSource(@RequestBody DataSourceUpdate dataSourceUpdate) throws DataVinesException {
        return dataSourceService.update(dataSourceUpdate)>0;
    }

    @ApiOperation(value = "delete databases")
    @DeleteMapping(value = "/{id}")
    public Object deleteDataSource(@PathVariable Long id)  {
        return dataSourceService.delete(id);
    }

    @ApiOperation(value = "list datasource by workspace id")
    @GetMapping(value = "list/{id}")
    public Object listByWorkSpaceId(@PathVariable Long id)  {
        return dataSourceService.listByWorkSpaceId(id);
    }

    @ApiOperation(value = "get databases")
    @GetMapping(value = "/{id}/databases")
    public Object getDatabaseList(@PathVariable Long id)  {
        return dataSourceService.getDatabaseList(id);
    }

    @ApiOperation(value = "get tables")
    @GetMapping(value = "/{id}/{database}/tables")
    public Object getTableList(@PathVariable Long id, @PathVariable String database)  {
        return dataSourceService.getTableList(id, database);
    }

    @ApiOperation(value = "get columns")
    @GetMapping(value = "/{id}/{database}/{table}/columns")
    public Object getColumnList(@PathVariable Long id, @PathVariable String database, @PathVariable String table)  {
        return dataSourceService.getColumnList(id, database, table);
    }
}
