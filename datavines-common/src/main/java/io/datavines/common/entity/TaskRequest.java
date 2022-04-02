package io.datavines.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import io.datavines.common.enums.TimeoutStrategy;

public class TaskRequest implements Serializable {

    private long id;

    private long taskId;

    private String taskUniqueId;

    private String taskName;

    private String executePlatformType;

    private String executePlatformParameter;

    private String engineType;

    private String engineParameter;

    private String applicationParameter;

    private String tenantCode;

    private Integer retryTimes;

    private Integer retryInterval;

    private Integer timeout;

    private TimeoutStrategy timeoutStrategy;

    private String executeHost;

    private Integer status;

    private String applicationId;

    private int processId;

    private String executeFilePath;

    private String logPath;

    private String env;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public TaskRequest(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getTaskUniqueId() {
        return taskUniqueId;
    }

    public void setTaskUniqueId(String taskUniqueId) {
        this.taskUniqueId = taskUniqueId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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

    public String getApplicationParameter() {
        return applicationParameter;
    }

    public void setApplicationParameter(String applicationParameter) {
        this.applicationParameter = applicationParameter;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
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

    public String getExecuteHost() {
        return executeHost;
    }

    public void setExecuteHost(String executeHost) {
        this.executeHost = executeHost;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public String getExecuteFilePath() {
        return executeFilePath;
    }

    public void setExecuteFilePath(String executeFilePath) {
        this.executeFilePath = executeFilePath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
