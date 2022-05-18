package io.datavines.server.coordinator.api.controller;

import io.datavines.common.dto.job.JobCreate;
import io.datavines.server.DataVinesConstants;
import io.datavines.server.coordinator.api.annotation.AuthIgnore;
import io.datavines.server.coordinator.api.aop.RefreshToken;
import io.datavines.server.coordinator.repository.service.JobService;
import io.datavines.server.exception.DataVinesServerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
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

    @AuthIgnore
    @ApiOperation(value = "Job create")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createJob(@Valid @RequestBody JobCreate jobCreate) throws DataVinesServerException {
        return jobService.createJob(jobCreate);
    }
}
