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
import io.datavines.server.api.dto.bo.token.TokenCreate;
import io.datavines.server.api.dto.bo.token.TokenUpdate;
import io.datavines.server.repository.service.AccessTokenService;
import io.datavines.server.utils.ContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(value = "token", tags = "token", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/token", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
public class AccessTokenController {

    @Resource
    private AccessTokenService accessTokenService;

    @ApiOperation(value = "create token")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createToken(@Valid @RequestBody TokenCreate tokenCreate) throws DataVinesServerException {
        return accessTokenService.create(tokenCreate);
    }

    @ApiOperation(value = "update token")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateToken(@Valid @RequestBody TokenUpdate tokenUpdate) throws DataVinesServerException {
        return accessTokenService.update(tokenUpdate);
    }

    @ApiOperation(value = "delete token")
    @DeleteMapping(value = "/{id}")
    public Object deleteToken(@PathVariable Long id)  {
        // 加入黑名单，并且需要拦截器进行处理
        return accessTokenService.deleteToken(id);
    }

    @ApiOperation(value = "page token")
    @GetMapping(value = "/page")
    public Object listByUserId(@RequestParam("workspaceId") Long workspaceId,
                               @RequestParam("pageNumber") Integer pageNumber,
                               @RequestParam("pageSize") Integer pageSize)  {
        return accessTokenService.page(workspaceId, ContextHolder.getUserId(), pageNumber, pageSize);
    }
}
