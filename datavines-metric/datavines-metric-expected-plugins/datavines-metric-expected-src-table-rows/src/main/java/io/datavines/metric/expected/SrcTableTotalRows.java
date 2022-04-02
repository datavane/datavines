package io.datavines.metric.expected;

import io.datavines.metric.api.ExpectedValue;

public class SrcTableTotalRows implements ExpectedValue {

    @Override
    public String getName() {
        return "expected_value";
    }

    @Override
    public String getType() {
        return "src_table_total_rows";
    }

    @Override
    public String getExecuteSql() {
        return "SELECT COUNT(*) AS expected_value FROM ${src_table} WHERE (${src_filter})";
    }

    @Override
    public String getOutputTable() {
        return "total_count";
    }

    @Override
    public boolean isNeedDefaultDatasource() {
        return false;
    }
}
