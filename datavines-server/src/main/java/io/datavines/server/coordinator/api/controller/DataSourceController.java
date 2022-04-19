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
import io.datavines.server.coordinator.api.dto.datasource.DataSourceCreate;
import io.datavines.server.coordinator.api.dto.datasource.DataSourceUpdate;
import io.datavines.server.coordinator.api.entity.ResultMap;
import io.datavines.server.coordinator.repository.entity.DataSource;
import io.datavines.server.coordinator.repository.service.DataSourceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Api(value = "/datasource", tags = "datasource", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/datasource", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @ApiOperation(value = "test connection")
    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object testConnection(@RequestBody TestConnectionRequestParam param)  {
        Map<String,Object> result = new HashMap<>();
        result.put("result", dataSourceService.testConnect(param));
        return new ResultMap().success().payload(result);
    }

    @ApiOperation(value = "create datasource")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createDataSource(@RequestBody DataSourceCreate dataSourceCreate)  {
        Map<String,Object> result = new HashMap<>();
        result.put("taskId", dataSourceService.insert(dataSourceCreate));
        return new ResultMap().success().payload(result);
    }

    @ApiOperation(value = "update datasource")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateDataSource(@RequestBody DataSourceUpdate dataSourceUpdate) throws DataVinesException {
        Map<String,Object> result = new HashMap<>();
        result.put("result", dataSourceService.update(dataSourceUpdate)>0);
        return new ResultMap().success().payload(result);
    }

    @ApiOperation(value = "get databases")
    @GetMapping(value = "/{id}")
    public Object getDatabaseList(@PathVariable Long id)  {
        Map<String,Object> result = new HashMap<>();
        result.put("taskId", dataSourceService.getDatabaseList(id));
        return new ResultMap().success().payload(result);
    }
}
