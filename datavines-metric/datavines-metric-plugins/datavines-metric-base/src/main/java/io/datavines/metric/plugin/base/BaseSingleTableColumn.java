package io.datavines.metric.plugin.base;

public abstract class BaseSingleTableColumn extends BaseSingleTable {

    public BaseSingleTableColumn() {
        super();
        configSet.add("column");
        requiredOptions.add("column");
    }

}
