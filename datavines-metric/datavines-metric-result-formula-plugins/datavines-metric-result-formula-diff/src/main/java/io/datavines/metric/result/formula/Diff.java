package io.datavines.metric.result.formula;

import io.datavines.metric.api.ResultFormula;

public class Diff implements ResultFormula {

    @Override
    public double getResult(double actualValue, double expectedValue) {
        return Math.abs(actualValue - expectedValue);
    }
}
