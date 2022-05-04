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

import io.datavines.common.dto.user.UserLogin;
import io.datavines.common.dto.user.UserRegister;
import io.datavines.common.exception.DataVinesException;
import io.datavines.server.DataVinesConstants;
import io.datavines.server.coordinator.api.annotation.AuthIgnore;
import io.datavines.server.coordinator.api.entity.ResultMap;
import io.datavines.server.coordinator.repository.service.UserService;
import io.datavines.server.utils.TokenManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(value = "login", tags = "login")
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH)
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenManager tokenManager;

    @AuthIgnore
    @ApiOperation(value = "login")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object login(@RequestBody UserLogin userLogin) throws DataVinesException {
        return new ResultMap(tokenManager)
                .successWithToken(userLogin.getUsername(), userLogin.getPassword())
                .payload(userService.login(userLogin));
    }

    @AuthIgnore
    @ApiOperation(value = "register")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object register(@RequestBody UserRegister userRegister) throws DataVinesException {
        Map<String,Object> result = new HashMap<>();
        result.put("result", userService.register(userRegister));
        return new ResultMap().success().payload(result);
    }

}
