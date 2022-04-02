package io.datavines.metric.expected;

import io.datavines.metric.api.ExpectedValue;

/**
 * 
 */
public class DailyAvg implements ExpectedValue {

    @Override
    public String getName() {
        return "day_range.day_avg";
    }

    @Override
    public String getType() {
        return "daily_avg";
    }

    @Override
    public String getExecuteSql() {
        return "select round(avg(actual_value),2) as day_avg from actual_values where data_time >=date_trunc('DAY', ${data_time}) and data_time < date_add(date_trunc('day', ${data_time}),1) and unique_code = ${unique_code} and actual_name = '${actual_name}'";
    }

    @Override
    public String getOutputTable() {
        return "day_range";
    }

    @Override
    public boolean isNeedDefaultDatasource() {
        return true;
    }
}
