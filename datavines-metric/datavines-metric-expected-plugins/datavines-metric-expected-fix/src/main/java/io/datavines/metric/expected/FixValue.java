package io.datavines.metric.expected;

import io.datavines.metric.api.ExpectedValue;

import java.util.Map;

public class FixValue implements ExpectedValue {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getType() {
        return "fix_value";
    }

    @Override
    public String getExecuteSql() {
        return null;
    }

    @Override
    public String getOutputTable() {
        return null;
    }

    @Override
    public boolean isNeedDefaultDatasource() {
        return false;
    }

    @Override
    public void prepare(Map<String, String> config) {

    }
}
