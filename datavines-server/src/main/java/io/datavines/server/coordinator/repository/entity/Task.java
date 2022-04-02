package io.datavines.server.coordinator.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.enums.TimeoutStrategy;
import io.datavines.server.enums.JobType;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("task")
public class Task implements Serializable {

    private static final long serialVersionUID = -1L;

    @TableId(type= IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "job_id")
    private Long jobId;

    @TableField(value = "job_type")
    private JobType jobType;

    @TableField(value = "datasource_id")
    private Long dataSourceId;

    @TableField(value = "execute_platform_type")
    private String executePlatformType;

    @TableField(value = "execute_platform_parameter")
    private String executePlatformParameter;

    @TableField(value = "engine_type")
    private String engineType;

    @TableField(value = "engine_parameter")
    private String engineParameter;

    /**
     * {@link TaskParameter}
     */
    @TableField(value = "parameter")
    private String parameter;

    @TableField(value = "status")
    private ExecutionStatus status;

    @TableField(value = "retry_times")
    private Integer retryTimes;

    @TableField(value = "retry_interval")
    private Integer retryInterval;

    @TableField(value = "timeout")
    private Integer timeout;

    @TableField(value = "timeout_strategy")
    private TimeoutStrategy timeoutStrategy = TimeoutStrategy.WARN;

    @TableField(value = "tenant_code")
    private String tenantCode;

    @TableField(value = "execute_host")
    private String executeHost;

    @TableField(value = "application_id")
    private String applicationId;

    @TableField(value = "application_tag")
    private String applicationIdTag;

    @TableField(value = "process_id")
    private int processId;

    @TableField(value = "execute_file_path")
    private String executeFilePath;

    @TableField(value = "log_path")
    private String logPath;

    @TableField(value = "env")
    private String env;

    @TableField(value = "submit_time")
    private LocalDateTime submitTime;

    @TableField(value = "start_time")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    private LocalDateTime endTime;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
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

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
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

    public String getExecuteHost() {
        return executeHost;
    }

    public void setExecuteHost(String executeHost) {
        this.executeHost = executeHost;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationIdTag() {
        return applicationIdTag;
    }

    public void setApplicationIdTag(String applicationIdTag) {
        this.applicationIdTag = applicationIdTag;
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

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
