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

import io.datavines.engine.api.engine.EngineExecutor;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.ResultFormula;
import io.datavines.metric.api.SqlMetric;
import io.datavines.server.DataVinesConstants;
import io.datavines.server.coordinator.api.aop.RefreshToken;
import io.datavines.server.coordinator.api.entity.Item;
import io.datavines.server.enums.OperatorType;
import io.datavines.spi.PluginLoader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Api(value = "metric", tags = "metric", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/metric", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class MetricController {

    @ApiOperation(value = "get metric list")
    @GetMapping(value = "/list")
    public Object getMetricList() {
        Set<String> metricList = PluginLoader.getPluginLoader(SqlMetric.class).getSupportedPlugins();
        List<Item> items = new ArrayList<>();
        metricList.forEach(it -> {
            Item item = new Item(it,it);
            items.add(item);
        });

        return items;
    }


    @ApiOperation(value = "get metric info")
    @GetMapping(value = "/configs/{name}")
    public Object getMetricConfig(@PathVariable("name") String name) {
        SqlMetric sqlMetric = PluginLoader.getPluginLoader(SqlMetric.class).getOrCreatePlugin(name);
        if (sqlMetric != null) {
            Set<String> resultSet = sqlMetric.getConfigSet();
            resultSet.remove("table");
            resultSet.remove("column");
            resultSet.remove("filter");
            List<Item> items = new ArrayList<>();
            resultSet.forEach(it -> {
                Item item = new Item(it,it);
                items.add(item);
            });
            return items;
        }

        return null;
    }

    @ApiOperation(value = "get expected value list")
    @GetMapping(value = "/expectedValue/list")
    public Object getExpectedTypeList() {
        Set<String> expectedValueList = PluginLoader.getPluginLoader(ExpectedValue.class).getSupportedPlugins();
        List<Item> items = new ArrayList<>();
        expectedValueList.forEach(it -> {
            Item item = new Item(it,it);
            items.add(item);
        });

        return items;
    }

    @ApiOperation(value = "get engine type list")
    @GetMapping(value = "/engine/list")
    public Object getEngineTypeList() {
        Set<String> engineTypeList = PluginLoader.getPluginLoader(EngineExecutor.class).getSupportedPlugins();
        List<Item> items = new ArrayList<>();
        engineTypeList.forEach(it -> {
            Item item = new Item(it,it);
            items.add(item);
        });

        return items;
    }

    @ApiOperation(value = "get result formula list")
    @GetMapping(value = "/resultFormula/list")
    public Object getResultFormulaList() {
        Set<String> resultFormulaTypeList = PluginLoader.getPluginLoader(ResultFormula.class).getSupportedPlugins();
        List<Item> items = new ArrayList<>();
        resultFormulaTypeList.forEach(it -> {
            Item item = new Item(it,it);
            items.add(item);
        });

        return items;
    }

    @ApiOperation(value = "get operator list")
    @GetMapping(value = "/operator/list")
    public Object getOperatorList() {
        List<Item> items = new ArrayList<>();
        items.add(new Item("=","eq"));
        items.add(new Item("<","lt"));
        items.add(new Item("<=","lte"));
        items.add(new Item(">","gt"));
        items.add(new Item(">=","gte"));
        items.add(new Item("!=","neq"));
        return items;
    }

}
