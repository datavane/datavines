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

import io.datavines.core.aop.RefreshToken;
import io.datavines.core.constant.DataVinesConstants;
import io.datavines.server.api.dto.bo.catalog.lineage.LineageEntityEdgeInfo;
import io.datavines.server.api.dto.bo.catalog.lineage.SqlWithDataSourceKeyProperties;
import io.datavines.server.api.dto.bo.catalog.lineage.SqlWithDataSourceList;
import io.datavines.server.repository.entity.catalog.CatalogTagCategory;
import io.datavines.server.repository.service.CatalogEntityRelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "catalog", tags = "catalog", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/catalog/lineage", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class CatalogLineageController {

    @Autowired
    private CatalogEntityRelService catalogEntityRelService;

    @ApiOperation(value = "add lineage", response = Long.class)
    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object addLineage(@Valid @RequestBody LineageEntityEdgeInfo entityEdgeInfo) {
        return catalogEntityRelService.addLineage(entityEdgeInfo);
    }

    @ApiOperation(value = "get lineage by full qualified name", response = CatalogTagCategory.class, responseContainer = "list")
    @GetMapping(value = "/getByFqn/{datasourceId}/{fqn}")
    public Object getByFqn(@PathVariable Long datasourceId, @PathVariable String fqn) {
        return catalogEntityRelService.getLineageByFqn(datasourceId,fqn,1,1);
    }

    @ApiOperation(value = "delete tag category", response = boolean.class)
    @GetMapping(value = "/getByUUID/{uuid}")
    public Object getByUUID(@PathVariable String uuid) {
        return catalogEntityRelService.getLineageByUUID(uuid,1,1);
    }

    @ApiOperation(value = "delete lineage", response = boolean.class)
    @DeleteMapping(value = "/{fromUUID}/{toUUID}")
    public Object deleteLineage(@PathVariable("fromUUID") String fromUUID,
                                     @PathVariable("toUUID") String toUUID) {
        return catalogEntityRelService.deleteLineage(fromUUID, toUUID);
    }

    @ApiOperation(value = "parse sql to get lineage", response = Long.class)
    @PostMapping(value = "/addByParseSql", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object addLineageByParseSql(@Valid @RequestBody SqlWithDataSourceList sqlWithDataSourceList) {
        return catalogEntityRelService.addLineageByParseSql(sqlWithDataSourceList);
    }

    @ApiOperation(value = "parse sql to get lineage", response = Long.class)
    @PostMapping(value = "/addByParseSql2", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object addLineageByParseSql2(@Valid @RequestBody SqlWithDataSourceKeyProperties sqlWithDataSourceKeyProperties) {
        return catalogEntityRelService.addLineageByParseSql2(sqlWithDataSourceKeyProperties);
    }
}
