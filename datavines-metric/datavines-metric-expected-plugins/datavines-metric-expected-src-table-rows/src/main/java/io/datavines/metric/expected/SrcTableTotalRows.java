package io.datavines.metric.expected;

import io.datavines.metric.api.ExpectedValue;

import java.util.Map;

public class SrcTableTotalRows implements ExpectedValue {

    private StringBuilder sql =
            new StringBuilder("select count(*) as expected_value from ${src_table}");

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
        return sql.toString();
    }

    @Override
    public String getOutputTable() {
        return "total_count";
    }

    @Override
    public boolean isNeedDefaultDatasource() {
        return false;
    }

    @Override
    public void prepare(Map<String, String> config) {
        if (config.containsKey("src_filter")) {
            if (sql.toString().contains("where")) {
                sql.append(" and ").append(config.get("src_filter"));
            } else {
                sql.append(" where ").append(config.get("src_filter"));
            }
        }
    }
}
