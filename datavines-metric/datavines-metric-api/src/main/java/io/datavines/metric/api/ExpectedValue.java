package io.datavines.metric.api;

import io.datavines.spi.SPI;

/**
 * 
 */
@SPI
public interface ExpectedValue {

    /**
     * get value name
     * @return String
     */
    String getName();

    /**
     * get value type
     * @return String
     */
    String getType();

    /**
     * get value execute sql
     * @return String
     */
    String getExecuteSql();

    /**
     * get output table name
     * @return String
     */
    String getOutputTable();

    /**
     * need to read actual value from system database
     * @return
     */
    boolean isNeedDefaultDatasource();

}
