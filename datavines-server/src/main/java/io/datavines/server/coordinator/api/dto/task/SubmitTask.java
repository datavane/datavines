package io.datavines.server.coordinator.api.dto.task;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.datavines.common.entity.TaskParameter;
import io.datavines.common.enums.TimeoutStrategy;

@NotNull(message = "SubmitTask cannot be null")
public class SubmitTask {

    @NotBlank(message = "task name cannot be empty")
    private String name;

    private String executePlatformType = "local";

    private Map<String,Object> executePlatformParameter;

    private String engineType = "spark";

    private Map<String,Object> engineParameter;

    /**
     * {@link TaskParameter}
     */
    private TaskParameter parameter;

    private Integer retryTimes = 1;

    private Integer retryInterval = 1000;

    private Integer timeout = 3600;

    private TimeoutStrategy timeoutStrategy = TimeoutStrategy.WARN;

    private String tenantCode;

    private String env;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutePlatformType() {
        return executePlatformType;
    }

    public void setExecutePlatformType(String executePlatformType) {
        this.executePlatformType = executePlatformType;
    }

    public Map<String,Object> getExecutePlatformParameter() {
        return executePlatformParameter;
    }

    public void setExecutePlatformParameter(Map<String,Object> executePlatformParameter) {
        this.executePlatformParameter = executePlatformParameter;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public Map<String,Object> getEngineParameter() {
        return engineParameter;
    }

    public void setEngineParameter(Map<String,Object> engineParameter) {
        this.engineParameter = engineParameter;
    }

    public TaskParameter getParameter() {
        return parameter;
    }

    public void setParameter(TaskParameter parameter) {
        this.parameter = parameter;
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

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public TimeoutStrategy getTimeoutStrategy() {
        return timeoutStrategy;
    }

    public void setTimeoutStrategy(TimeoutStrategy timeoutStrategy) {
        this.timeoutStrategy = timeoutStrategy;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
