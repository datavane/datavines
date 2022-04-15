package io.datavines.metric.plugin;

import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.plugin.base.BaseSingleTableColumn;

import java.util.Map;
import java.util.Set;

public class ColumnInEnums extends BaseSingleTableColumn {

    public ColumnInEnums(){
        super();
        configSet.add("enum_list");

        requiredOptions.add("enum_list");
    }

    @Override
    public String getName() {
        return "column_in_enums";
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
            filters.add(" (${column} in ( ${enum_list} )) ");
        }
        super.prepare(config);
    }

    @Override
    public Set<String> getConfigSet() {
        return configSet;
    }
}
