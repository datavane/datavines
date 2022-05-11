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
package io.datavines.server.coordinator.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("dv_task_result")
public class TaskResult implements Serializable {

    private static final long serialVersionUID = -1L;

    @TableId(type= IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "metric_name")
    private String metricName;

    @TableField(value = "metric_dimension")
    private String metricDimension;

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

    @TableField(value = "result_formula")
    private String resultFormula;

    @TableField(value = "operator")
    private String operator;

    @TableField(value = "threshold")
    private Double threshold;

    @TableField(value = "failure_strategy")
    private String failureStrategy;

    @TableField(value = "state")
    private String state;

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

    public String getMetricDimension() {
        return metricDimension;
    }

    public void setMetricDimension(String metricDimension) {
        this.metricDimension = metricDimension;
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

    public String getResultFormula() {
        return resultFormula;
    }

    public void setResultFormula(String resultFormula) {
        this.resultFormula = resultFormula;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public String getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(String failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
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
