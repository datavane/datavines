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
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.api.annotation.CheckTokenExist;
import io.datavines.server.api.dto.vo.JobExecutionResultVO;
import io.datavines.server.repository.service.JobExecutionResultService;
import io.datavines.server.repository.service.JobExecutionService;
import io.datavines.server.repository.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(value = "openapi", tags = "openapi", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/openapi", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
@Validated
public class OpenApiController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private JobExecutionResultService jobExecutionResultService;

    @CheckTokenExist
    @ApiOperation(value = "execute job")
    @PostMapping(value = "/job/execute/{id}")
    public Object executeJob(@PathVariable("id") Long jobId) throws DataVinesServerException {
        return jobService.execute(jobId, null);
    }

    @CheckTokenExist
    @ApiOperation(value = "kill job", response = Long.class)
    @PostMapping(value = "/job/execution/kill/{executionId}")
    public Object kill(@PathVariable("executionId") Long executionId) {
        return jobExecutionService.killJob(executionId);
    }

    @CheckTokenExist
    @ApiOperation(value = "get job execution status", response = String.class)
    @GetMapping(value = "/job/execution/status/{executionId}")
    public Object getTaskStatus(@PathVariable("executionId") Long executionId) {
        return jobExecutionService.getById(executionId).getStatus();
    }

    @CheckTokenExist
    @Deprecated
    @ApiOperation(value = "get job execution result", response = JobExecutionResultVO.class)
    @GetMapping(value = "/job/execution/result/{executionId}")
    public Object getJobExecutionResultInfo(@PathVariable("executionId") Long executionId) {
        return jobExecutionResultService.getCheckResultByJobExecutionId(executionId);
    }
}
