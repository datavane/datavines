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

import io.datavines.server.DataVinesConstants;
import io.datavines.server.coordinator.api.aop.RefreshToken;
import io.datavines.server.coordinator.api.entity.Item;
import io.datavines.server.coordinator.api.entity.dto.tenant.TenantCreate;
import io.datavines.server.coordinator.api.entity.dto.tenant.TenantUpdate;
import io.datavines.server.coordinator.api.entity.dto.tenant.TenantCreate;
import io.datavines.server.coordinator.api.entity.dto.tenant.TenantUpdate;
import io.datavines.server.coordinator.repository.entity.Env;
import io.datavines.server.coordinator.repository.entity.Tenant;
import io.datavines.server.coordinator.repository.service.TenantService;
import io.datavines.server.coordinator.repository.service.TenantService;
import io.datavines.server.exception.DataVinesServerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(value = "tenant", tags = "tenant", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/tenant", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @ApiOperation(value = "create tenant")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createTenant(@RequestBody TenantCreate tenantCreate) throws DataVinesServerException {
        return tenantService.create(tenantCreate);
    }

    @ApiOperation(value = "update tenant")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateTenant(@RequestBody TenantUpdate tenantUpdate) throws DataVinesServerException {
        return tenantService.update(tenantUpdate)>0;
    }

    @ApiOperation(value = "delete tenant")
    @DeleteMapping(value = "/{id}")
    public Object deleteTenant(@PathVariable Long id)  {
        return tenantService.deleteById(id);
    }

    @ApiOperation(value = "list tenant by user id")
    @GetMapping(value = "list")
    public Object listByUserId()  {
        return tenantService.listByUserId();
    }

    @ApiOperation(value = "list env options by user id")
    @GetMapping(value = "listOptions")
    public Object listOptions()  {
        List<Tenant> tenantList = tenantService.listByUserId();
        List<Item> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tenantList)) {
            tenantList.forEach(it -> {
                Item item = new Item(it.getTenant(), it.getId()+"");
                items.add(item);
            });
        }

        return items;
    }
}
