package io.datavines.metric.result.formula;

import io.datavines.metric.api.ResultFormula;

public class Percentage implements ResultFormula {

    @Override
    public double getResult(double actualValue, double expectedValue) {
        double result = 0;
        if (expectedValue > 0) {
            result = actualValue / expectedValue * 100;
        }

        return result;
    }
}
