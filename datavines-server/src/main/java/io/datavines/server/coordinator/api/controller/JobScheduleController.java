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
import io.datavines.server.coordinator.api.entity.dto.job.JobCreate;
import io.datavines.server.coordinator.api.entity.dto.job.JobUpdate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleCreate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleUpdate;
import io.datavines.server.coordinator.repository.service.JobScheduleService;
import io.datavines.server.coordinator.repository.service.JobService;
import io.datavines.server.exception.DataVinesServerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "job", tags = "job", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/job/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
@Validated
public class JobScheduleController {

    @Autowired
    private JobScheduleService jobScheduleService;

    @ApiOperation(value = "create update job")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createJob(@Valid @RequestBody JobScheduleCreate jobScheduleCreate) throws DataVinesServerException {

        return jobScheduleService.create(jobScheduleCreate);
    }

    @ApiOperation(value = "delete job")
    @DeleteMapping(value = "/{id}")
    public Object deleteJob(@PathVariable Long id)  {
        return jobScheduleService.deleteById(id);
    }

    @ApiOperation(value = "update job")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateJob(@Valid @RequestBody JobScheduleUpdate jobScheduleUpdate) throws DataVinesServerException {
        return jobScheduleService.update(jobScheduleUpdate);
    }

    @ApiOperation(value = "get job by id")
    @GetMapping(value = "/{id}")
    public Object getById(@PathVariable Long id)  {
        return jobScheduleService.getById(id);
    }


}
