package io.datavines.metric.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.datavines.common.config.CheckResult;
import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.api.SqlMetric;

public class CustomSql implements SqlMetric {

    @Override
    public String getName() {
        return "custom_sql";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.ACCURACY;
    }

    @Override
    public MetricType getType() {
        return MetricType.SINGLE_TABLE_CUSTOM_SQL;
    }

    @Override
    public String getInvalidateItemsSql() {
        return null;
    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return false;
    }

    @Override
    public String getActualValueSql() {
        return null;
    }

    @Override
    public CheckResult validateConfig(Map<String, String> config) {
        return null;
    }

    @Override
    public void prepare(Map<String, String> config) {

    }

    @Override
    public List<String> getConfigList() {
        return Collections.emptyList();
    }
}
