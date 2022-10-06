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
package io.datavines.server.api.dto.bo.task;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.datavines.common.entity.TaskParameter;
import io.datavines.common.enums.TimeoutStrategy;
import lombok.Data;

@Data
@NotNull(message = "SubmitTask cannot be null")
public class SubmitTask {

    @NotBlank(message = "task name cannot be empty")
    private String name;

    private String executePlatformType = "local";

    private Map<String,Object> executePlatformParameter;

    private String engineType = "jdbc";

    private Map<String,Object> engineParameter;

    private String errorDataStorageType = "";

    private Map<String,Object> errorDataStorageParameter;

    private TaskParameter parameter;

    private Integer retryTimes = 0;

    private Integer retryInterval = 1000;

    private Integer timeout = 3600;

    private TimeoutStrategy timeoutStrategy = TimeoutStrategy.WARN;

    private String tenantCode;

    private String env;
}
