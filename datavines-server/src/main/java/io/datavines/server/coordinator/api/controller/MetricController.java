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

import io.datavines.metric.api.SqlMetric;
import io.datavines.server.DataVinesConstants;
import io.datavines.server.coordinator.api.aop.RefreshToken;
import io.datavines.spi.PluginLoader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Api(value = "metric", tags = "metric", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/metric", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class MetricController {

    @ApiOperation(value = "get metric list")
    @GetMapping(value = "/list")
    public Object getMetricList() {
        return PluginLoader.getPluginLoader(SqlMetric.class).getSupportedPlugins();
    }

    @ApiOperation(value = "get metric info")
    @GetMapping(value = "/info/{name}")
    public Object getMetricInfo(@PathVariable("name") String name) {
        return PluginLoader.getPluginLoader(SqlMetric.class).getOrCreatePlugin(name);
    }

}
