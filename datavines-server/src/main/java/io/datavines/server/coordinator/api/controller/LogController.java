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

import io.datavines.core.constant.DataVinesConstants;
import io.datavines.core.aop.RefreshToken;
import io.datavines.server.coordinator.server.log.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(value = "log", tags = "log")
@RestController
@RefreshToken
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/log")
public class LogController {

    @Resource
    private LogService logService;

    @ApiOperation(value = "queryWholeLog", notes = "query whole log's entry")
    @GetMapping(value = "/queryWholeLog")
    public void queryWholeLog(@RequestParam("taskId") Long taskId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String taskHost = logService.getTaskHost(taskId);
        response.sendRedirect(request.getScheme()+ "://" + taskHost + "/api/v1/log/queryWholeLogExecute?taskId=" + taskId);
    }

    @ApiOperation(value = "queryWholeLogExecute", notes = "query whole log really execute by taskId")
    @GetMapping(value = "/queryWholeLogExecute")
    public Object queryWholeLogExecute(@RequestParam("taskId") Long taskId) {
        return logService.queryWholeLog(taskId);
    }
}
