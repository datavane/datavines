package io.datavines.server.coordinator.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("task_result")
public class TaskResult implements Serializable {

    private static final long serialVersionUID = -1L;

    @TableId(type= IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "metric_name")
    private String metricName;

    @TableField(value = "metric_type")
    private String metricType;

    @TableField(value = "task_id")
    private Long taskId;

    @TableField(value = "actual_value")
    private Double actualValue;

    @TableField(value = "expected_value")
    private Double expectedValue;

    @TableField(value = "expected_type")
    private String expectedType;

    @TableField(value = "check_type")
    private Integer checkType;

    @TableField(value = "operator")
    private Integer operator;

    @TableField(value = "threshold")
    private Double threshold;

    @TableField(value = "failure_strategy")
    private Integer failureStrategy;

    @TableField(value = "state")
    private Integer state;

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

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Double getActualValue() {
        return actualValue;
    }

    public void setActualValue(Double actualValue) {
        this.actualValue = actualValue;
    }

    public Double getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(Double expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public void setExpectedType(String expectedType) {
        this.expectedType = expectedType;
    }

    public Integer getCheckType() {
        return checkType;
    }

    public void setCheckType(Integer checkType) {
        this.checkType = checkType;
    }

    public Integer getOperator() {
        return operator;
    }

    public void setOperator(Integer operator) {
        this.operator = operator;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Integer getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(Integer failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
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
