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
    @GetMapping(value = "/{name}")
    public Object getMetricInfo(@PathVariable("name") String name) {
        Map<String,Object> result = new HashMap<>();
        result.put("metricInfo", PluginLoader.getPluginLoader(SqlMetric.class).getOrCreatePlugin(name));
        return new ResultMap().success().payload(result);
    }

}
