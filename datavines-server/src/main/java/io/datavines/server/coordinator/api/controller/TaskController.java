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

import javax.validation.Valid;

import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.api.aop.RefreshToken;
import io.datavines.server.coordinator.api.entity.ResultMap;
import io.datavines.server.coordinator.repository.service.TaskResultService;
import io.datavines.server.exception.DataVinesServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.datavines.server.DataVinesConstants;

import io.datavines.common.dto.task.SubmitTask;
import io.datavines.server.coordinator.repository.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

@Api(value = "task", tags = "task", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/task", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskResultService taskResultService;

    @ApiOperation(value = "submit task")
    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object submitTask(@Valid @RequestBody SubmitTask submitTask) throws DataVinesServerException {
        return taskService.submitTask(submitTask);
    }

    @ApiOperation(value = "kill task")
    @DeleteMapping(value = "/kill/{id}")
    public Object killTask(@PathVariable("id") Long taskId) {
        return taskService.killTask(taskId);
    }

    @ApiOperation(value = "get task status")
    @GetMapping(value = "/status/{id}")
    public Object getTaskStatus(@PathVariable("id") Long taskId) {
        return taskService.getById(taskId).getStatus().getDescription();
    }

    @ApiOperation(value = "get task result")
    @GetMapping(value = "result/{id}")
    public Object getTaskResultInfo(@PathVariable("id") Long taskId) {
        return taskResultService.getByTaskId(taskId);
    }
}
