package io.datavines.metric.result.formula;

import io.datavines.metric.api.ResultFormula;

public class DiffPercentage implements ResultFormula {

    @Override
    public double getResult(double actualValue, double expectedValue) {
        double result = 0;
        if (expectedValue > 0) {
            result = Math.abs(expectedValue - actualValue) / expectedValue * 100;
        }

        return result;
    }
}
