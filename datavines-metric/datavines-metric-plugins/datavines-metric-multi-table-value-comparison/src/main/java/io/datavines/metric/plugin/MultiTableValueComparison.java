package io.datavines.metric.plugin;

import io.datavines.common.config.CheckResult;
import io.datavines.common.entity.ExecuteSql;
import io.datavines.metric.api.ConfigItem;
import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.api.SqlMetric;

import java.util.Map;

public class MultiTableValueComparison implements SqlMetric {

    @Override
    public String getName() {
        return "multi_table_value_comparison";
    }

    @Override
    public String getZhName() {
        return "两表值比对";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.ACCURACY;
    }

    @Override
    public MetricType getType() {
        return MetricType.MULTI_TABLE_VALUE_COMPARISON;
    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return false;
    }

    @Override
    public ExecuteSql getInvalidateItems() {
        return null;
    }

    @Override
    public ExecuteSql getActualValue() {
        return null;
    }

    @Override
    public CheckResult validateConfig(Map<String, Object> config) {
        return null;
    }

    @Override
    public Map<String, ConfigItem> getConfigMap() {
        return null;
    }

    @Override
    public void prepare(Map<String, String> config) {

    }
}
