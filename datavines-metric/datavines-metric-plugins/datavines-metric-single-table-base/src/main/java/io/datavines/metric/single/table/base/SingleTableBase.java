package io.datavines.metric.single.table.base;

import io.datavines.common.entity.ExecuteSql;
import io.datavines.metric.api.SqlMetric;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SingleTableBase implements SqlMetric {

    protected String invalidateItemsSqlWrapper() {
        String sql = getInvalidateItemsSql();

        if (!StringUtils.isEmpty(sql)) {
            if (sql.toLowerCase().contains("where")) {
                sql += " AND (${src_filter})";
            } else {
                sql += " WHERE (${src_filter})";
            }
        }

        return sql;
    }

    @Override
    public ExecuteSql getInvalidateItems() {
        ExecuteSql executeSql = new ExecuteSql();
        executeSql.setResultTable("invalidate_items");
        executeSql.setSql(invalidateItemsSqlWrapper());
        executeSql.setErrorOutput(isInvalidateItemsCanOutput());
        return executeSql;
    }

    @Override
    public List<String> getConfigList() {

        List<String> list = new ArrayList<>();
        list.add("src_table");
        list.add("src_filter");
        list.add("src_field");
        return list;
    }

    @Override
    public void prepare(Map<String, String> config) {
        if (!config.containsKey("src_filter")) {
            config.put("src_filter", "1=1");
        }
    }
}