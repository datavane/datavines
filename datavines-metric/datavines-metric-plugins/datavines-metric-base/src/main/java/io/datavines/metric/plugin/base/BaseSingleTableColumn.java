package io.datavines.metric.plugin.base;

public abstract class BaseSingleTableColumn extends BaseSingleTable {

    static {
        configSet.add("column");

        REQUIRED_OPTIONS.add("column");
    }
}
