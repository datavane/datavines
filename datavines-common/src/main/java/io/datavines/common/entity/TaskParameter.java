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

    private String resultFormula = "percentage";

    private String operator = "gt";

    private double threshold = 0;

    private String failureStrategy = "none";

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

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(String failureStrategy) {
        this.failureStrategy = failureStrategy;
    }
}
