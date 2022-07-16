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
package io.datavines.server.coordinator.api.dto.bo.job;

import io.datavines.common.enums.TimeoutStrategy;

import javax.validation.constraints.NotNull;

@NotNull(message = "JobCreate cannot be null")
public class JobCreate {

    @NotNull(message = "Job type cannot be empty")
    private String type;

    @NotNull(message = "Datasource cannot be empty")
    private long dataSourceId;

    private String executePlatformType = "local";

    private String executePlatformParameter;

    private String engineType = "jdbc";

    private String engineParameter;

    /**
     * Job Parameters 根据 jobType 来进行参数转换
     */
    private String parameter;

    private int timeout = 60000;

    private TimeoutStrategy timeoutStrategy = TimeoutStrategy.WARN;

    private Integer retryTimes = 0;

    private Integer retryInterval = 1000;

    private Long tenantCode;

    private Long env;

    /**
     * 1:running now, 0:don't run
     */
    private int runningNow;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public TimeoutStrategy getTimeoutStrategy() {
        return timeoutStrategy;
    }

    public void setTimeoutStrategy(TimeoutStrategy timeoutStrategy) {
        this.timeoutStrategy = timeoutStrategy;
    }

    public String getExecutePlatformType() {
        return executePlatformType;
    }

    public void setExecutePlatformType(String executePlatformType) {
        this.executePlatformType = executePlatformType;
    }

    public String getExecutePlatformParameter() {
        return executePlatformParameter;
    }

    public void setExecutePlatformParameter(String executePlatformParameter) {
        this.executePlatformParameter = executePlatformParameter;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getEngineParameter() {
        return engineParameter;
    }

    public void setEngineParameter(String engineParameter) {
        this.engineParameter = engineParameter;
    }

    public Long getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(Long tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Long getEnv() {
        return env;
    }

    public void setEnv(Long env) {
        this.env = env;
    }

    public int getRunningNow() {
        return runningNow;
    }

    public void setRunningNow(int runningNow) {
        this.runningNow = runningNow;
    }
}
