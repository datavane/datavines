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
import io.datavines.server.coordinator.api.dto.task.SubmitTask;
import io.datavines.server.coordinator.api.entity.ResultMap;
import io.datavines.server.coordinator.repository.service.TaskResultService;
import io.datavines.server.coordinator.repository.service.TaskService;
import io.datavines.spi.PluginLoader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(value = "/metric", tags = "metric", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/metric", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MetricController {

    @ApiOperation(value = "get metric list")
    @GetMapping(value = "/list")
    public Object getMetricList() {
        Map<String,Object> result = new HashMap<>();
        result.put("metrics", PluginLoader.getPluginLoader(SqlMetric.class).getSupportedPlugins());
        return new ResultMap().success().payload(result);
    }

    @ApiOperation(value = "get metric info")
    @GetMapping(value = "/info/{name}")
    public Object getMetricInfo(@PathVariable("name") String name) {
        Map<String,Object> result = new HashMap<>();
        result.put("metricInfo", PluginLoader.getPluginLoader(SqlMetric.class).getOrCreatePlugin(name));
        return new ResultMap().success().payload(result);
    }

}
