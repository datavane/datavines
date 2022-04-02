package io.datavines.common.entity;

import java.util.Map;

import javax.validation.constraints.NotNull;

@NotNull(message = "TaskParameter cannot be null")
public class TaskParameter {

    private String metricType;

    private Map<String,Object> metricParameter;

    private ConnectorParameter srcConnectorParameter;

    private ConnectorParameter targetConnectorParameter;

    private String expectedType = "src_table_total_rows";

    private Map<String, Object> expectedParameter;

    private int checkType = 2;

    private int operator = 1;

    private int threshold = 0;

    private int failureStrategy = 1;

    public ConnectorParameter getSrcConnectorParameter() {
        return srcConnectorParameter;
    }

    public void setSrcConnectorParameter(ConnectorParameter srcConnectorParameter) {
        this.srcConnectorParameter = srcConnectorParameter;
    }

    public ConnectorParameter getTargetConnectorParameter() {
        return targetConnectorParameter;
    }

    public void setTargetConnectorParameter(ConnectorParameter targetConnectorParameter) {
        this.targetConnectorParameter = targetConnectorParameter;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public Map<String, Object> getMetricParameter() {
        return metricParameter;
    }

    public void setMetricParameter(Map<String, Object> metricParameter) {
        this.metricParameter = metricParameter;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public void setExpectedType(String expectedType) {
        this.expectedType = expectedType;
    }

    public Map<String, Object> getExpectedParameter() {
        return expectedParameter;
    }

    public void setExpectedParameter(Map<String, Object> expectedParameter) {
        this.expectedParameter = expectedParameter;
    }

    public int getCheckType() {
        return checkType;
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(int failureStrategy) {
        this.failureStrategy = failureStrategy;
    }
}
