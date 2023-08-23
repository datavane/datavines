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
package io.datavines.server.api.controller;

import io.datavines.common.param.TestConnectionRequestParam;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.core.aop.RefreshToken;
import io.datavines.core.constant.DataVinesConstants;
import io.datavines.server.api.dto.bo.storage.ErrorDataStorageCreate;
import io.datavines.server.api.dto.bo.storage.ErrorDataStorageUpdate;
import io.datavines.server.api.dto.vo.Item;
import io.datavines.server.repository.service.ErrorDataStorageService;
import io.datavines.spi.PluginLoader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.datavines.common.ConfigConstants.TRUE;

@Api(value = "errorDataStorage", tags = "errorDataStorage", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/errorDataStorage", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class ErrorDataStorageController {

    @Autowired
    private ErrorDataStorageService errorDataStorageService;

    @ApiOperation(value = "test error data storage")
    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object testConnection(@Valid @RequestBody TestConnectionRequestParam param)  {
        return errorDataStorageService.testConnect(param);
    }

    @ApiOperation(value = "create error data storage")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createErrorDataStorage(@Valid @RequestBody ErrorDataStorageCreate errorDataStorageCreate)  {
        return errorDataStorageService.create(errorDataStorageCreate);
    }

    @ApiOperation(value = "update error data storage")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateErrorDataStorage(@Valid @RequestBody ErrorDataStorageUpdate errorDataStorageUpdate) {
        return errorDataStorageService.update(errorDataStorageUpdate)>0;
    }

    @ApiOperation(value = "delete error data storage")
    @DeleteMapping(value = "/{id}")
    public Object deleteErrorDataStorage(@PathVariable Long id)  {
        return errorDataStorageService.deleteById(id);
    }

    @ApiOperation(value = "list error data storage by workspace id")
    @GetMapping(value = "list/{workspaceId}")
    public Object listByUserId(@PathVariable Long workspaceId) {
        return errorDataStorageService.listByWorkspaceId(workspaceId);
    }

    @ApiOperation(value = "get storage config json")
    @GetMapping(value = "/config/{type}")
    public Object getConfigJson(@PathVariable String type){
        return errorDataStorageService.getConfigJson(type);
    }

    @ApiOperation(value = "get error data storage type list")
    @GetMapping(value = "/type/list")
    public Object getErrorDataStorageTypeList() {
        Set<String> errorDataStorageList = PluginLoader.getPluginLoader(ConnectorFactory.class).getSupportedPlugins();
        List<Item> items = new ArrayList<>();
        for (String errorDataStorage : errorDataStorageList) {
            ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(errorDataStorage);
            if (connectorFactory  == null) {
                continue;
            }

            if (connectorFactory.getDialect().supportToBeErrorDataStorage()) {
                Item item = new Item(errorDataStorage,errorDataStorage);
                items.add(item);
            }
        }

        return items;
    }
}
