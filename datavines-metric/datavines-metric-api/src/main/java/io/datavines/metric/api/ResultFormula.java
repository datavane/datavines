package io.datavines.metric.api;

import io.datavines.spi.SPI;

@SPI
public interface ResultFormula {

    double getResult(double actualValue, double expectedValue);
}
