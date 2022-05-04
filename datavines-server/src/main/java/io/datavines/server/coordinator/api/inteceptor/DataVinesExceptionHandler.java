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

package io.datavines.server.coordinator.api.inteceptor;

import io.datavines.server.coordinator.api.entity.ResultMap;
import io.datavines.server.coordinator.api.enums.ApiStatus;
import io.datavines.server.exception.DataVinesServerException;
import io.datavines.server.utils.TokenManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class DataVinesExceptionHandler {

    @Resource
    private TokenManager tokenManager;

    @ExceptionHandler(DataVinesServerException.class)
    public ResponseEntity<ResultMap> dataVinesServerExceptionHandler(DataVinesServerException e, HttpServletRequest request) {
        ResultMap resultMap = new ResultMap(tokenManager);
        ApiStatus status = e.getStatus();
        if (ApiStatus.INVALID_TOKEN.equals(status)){
            return ResponseEntity.ok(new ResultMap().fail(ApiStatus.INVALID_TOKEN.getCode()).message("invalid token！"));
        }
        if (!Objects.isNull(status) ) {
            resultMap.fail(status.getCode());
        }
        resultMap.failAndRefreshToken(request);
        resultMap.message(e.getMessage());
        return ResponseEntity.ok(resultMap);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResultMap> constraintViolationExceptionHandler(Exception e, HttpServletRequest request) {
        ResultMap resultMap = new ResultMap(tokenManager);
        resultMap.failAndRefreshToken(request);
        resultMap.message(buildValidFailMessage((ConstraintViolationException) e));
        return ResponseEntity.ok(resultMap);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultMap> commonExceptionHandler(Exception e, HttpServletRequest request) {
        ResultMap resultMap = new ResultMap(tokenManager);
        resultMap.failAndRefreshToken(request);
        resultMap.message(e.getMessage());
        return ResponseEntity.ok(resultMap);
    }

    public String buildValidFailMessage(ConstraintViolationException violationException) {
        Set<ConstraintViolation<?>> constraintViolationSet = violationException.getConstraintViolations();
        StringBuilder messageBuilder = new StringBuilder();
        if(CollectionUtils.isEmpty(constraintViolationSet)){
            messageBuilder.append("invalid param");
        }
        Iterator<ConstraintViolation<?>> iterator = constraintViolationSet.iterator();
        while (iterator.hasNext()) {
            ConstraintViolation<?> next = iterator.next();
            messageBuilder.append(next.getMessage());
            if (iterator.hasNext()) {
                messageBuilder.append(System.lineSeparator());
            }
        }
        return messageBuilder.toString();
    }
}
