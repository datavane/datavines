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

import io.datavines.core.aop.RefreshToken;
import io.datavines.core.constant.DataVinesConstants;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.coordinator.api.entity.Item;
import io.datavines.server.coordinator.api.entity.dto.env.EnvCreate;
import io.datavines.server.coordinator.api.entity.dto.env.EnvUpdate;
import io.datavines.server.coordinator.repository.entity.Env;
import io.datavines.server.coordinator.repository.service.EnvService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(value = "env", tags = "env", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/env", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class EnvController {

    @Autowired
    private EnvService envService;

    @ApiOperation(value = "create env")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createEnv(@RequestBody EnvCreate envCreate) throws DataVinesServerException {
        return envService.create(envCreate);
    }

    @ApiOperation(value = "update env")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateEnv(@RequestBody EnvUpdate envUpdate) throws DataVinesServerException {
        return envService.update(envUpdate)>0;
    }

    @ApiOperation(value = "delete env")
    @DeleteMapping(value = "/{id}")
    public Object deleteEnv(@PathVariable Long id)  {
        return envService.deleteById(id);
    }

    @ApiOperation(value = "list env by user id")
    @GetMapping(value = "list")
    public Object listByUserId()  {
        return envService.listByUserId();
    }

    @ApiOperation(value = "list env options by user id")
    @GetMapping(value = "listOptions")
    public Object listOptions()  {
        List<Env> envList = envService.listByUserId();
        List<Item> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(envList)) {
            envList.forEach(it -> {
                Item item = new Item(it.getName(),it.getId()+"");
                items.add(item);
            });
        }

        return items;
    }
}
