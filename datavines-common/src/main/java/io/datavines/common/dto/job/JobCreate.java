package io.datavines.common.dto.job;

import io.datavines.common.entity.TaskParameter;
import io.datavines.common.enums.TimeoutStrategy;

import javax.validation.constraints.NotNull;

@NotNull(message = "JobCreate cannot be null")
public class JobCreate {

    @NotNull(message = "Job name cannot be empty")
    private String name;

    @NotNull(message = "Job type cannot be empty")
    private String type;

    @NotNull(message = "Datasource cannot be empty")
    private long dataSourceId;

    private int timeout = 60000;

    private TimeoutStrategy timeoutStrategy = TimeoutStrategy.WARN;

    private String executePlatformType;

    private String executePlatformParameter;

    private String engineType = "jdbc";

    private String engineParameter;

    private String tenantCode;

    private String env;

    /**
     * 1:running now, 0:don't run
     */
    private int runningNow;

    private long createBy;

    private long updateBy;

    /**
     * Task Parameters
     */
    private TaskParameter parameter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(long createBy) {
        this.createBy = createBy;
    }

    public long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(long updateBy) {
        this.updateBy = updateBy;
    }

    public TaskParameter getParameter() {
        return parameter;
    }

    public void setParameter(TaskParameter parameter) {
        this.parameter = parameter;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public int getRunningNow() {
        return runningNow;
    }

    public void setRunningNow(int runningNow) {
        this.runningNow = runningNow;
    }
}
