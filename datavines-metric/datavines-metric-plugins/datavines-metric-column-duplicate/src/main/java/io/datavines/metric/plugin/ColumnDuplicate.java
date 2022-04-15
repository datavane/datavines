package io.datavines.metric.plugin;

import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.plugin.base.BaseSingleTableColumn;

import java.util.Map;
import java.util.Set;

public class ColumnDuplicate extends BaseSingleTableColumn {

    public ColumnDuplicate(){
        super();
    }

    @Override
    public String getName() {
        return "column_duplicate";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.UNIQUENESS;
    }

    @Override
    public MetricType getType() {
        return MetricType.SINGLE_TABLE;
    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return true;
    }

    @Override
    public void prepare(Map<String, String> config) {

        if (config.containsKey("filter")) {
            invalidateItemsSql.append(" where ").append(config.get("filter"));
        }

        if (config.containsKey("column")) {
            invalidateItemsSql.append(" group by ${column} having count(*) > 1");
        }

    }

    @Override
    public Set<String> getConfigSet() {
        return configSet;
    }
}
