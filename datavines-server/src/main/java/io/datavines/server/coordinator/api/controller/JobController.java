package io.datavines.server.coordinator.api.controller;

import io.datavines.common.dto.job.JobCreate;
import io.datavines.server.DataVinesConstants;
import io.datavines.server.coordinator.api.aop.RefreshToken;
import io.datavines.server.coordinator.repository.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(value = "job", tags = "job", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/job", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class JobController {

    @Autowired
    private JobService jobService;

    @ApiOperation(value = "Job create")
    public Object createJob(@Valid @RequestBody JobCreate jobCreate) {
        return jobService.createJob(jobCreate);
    }
}
