package io.datavines.metric.plugin;

import java.util.List;
import java.util.Map;

import io.datavines.common.config.CheckResult;
import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.api.SqlMetric;

/**
 * 
 */
public class MultiTableAccuracy implements SqlMetric {

    @Override
    public String getName() {
        return "MultiTableAccuracy";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.ACCURACY;
    }

    @Override
    public MetricType getType() {
        return MetricType.MULTI_TABLE_ACCURACY;
    }

    @Override
    public String getInvalidateItemsSql() {
        return "SELECT ${src_table}.* FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) ${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) ${target_table} ON ${on_clause} WHERE ${where_clause}";
    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return true;
    }

    @Override
    public String getActualValueSql() {
        return "SELECT COUNT(*) AS invalidate_count from invalidate_items";
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
        return null;
    }
}
