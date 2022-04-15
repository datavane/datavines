package io.datavines.metric.plugin;

import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.plugin.base.BaseSingleTableColumn;

import java.util.Map;
import java.util.Set;

public class ColumnNotInEnums extends BaseSingleTableColumn {

    public ColumnNotInEnums(){
        super();
        configSet.add("enum_list");

        requiredOptions.add("enum_list");
    }

    @Override
    public String getName() {
        return "column_not_in_enums";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.EFFECTIVENESS;
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
        if (config.containsKey("enum_list") && config.containsKey("column")) {
            filters.add(" (${column} not in ( ${enum_list} ) or ${column} is null) ");
        }
        super.prepare(config);
    }

    @Override
    public Set<String> getConfigSet() {
        return configSet;
    }
}
