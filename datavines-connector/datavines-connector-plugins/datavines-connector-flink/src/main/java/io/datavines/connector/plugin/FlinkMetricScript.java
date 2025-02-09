/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.connector.plugin;

public class FlinkMetricScript extends JdbcMetricScript {

    @Override
    public String histogramActualValue(String uniqueKey, String where) {
        return "select concat(k, '\\001', cast(count as string)) as actual_value_" + uniqueKey +
                " from (select if(${column} is null, 'NULL', cast(${column} as string)) as k, count(1) as count from ${table} " +
                where + " group by ${column} order by count desc limit 50) T ";
    }

    @Override
    public String columnMatchRegex() {
        return " regexp(${column}, ${regex}) ";
    }

    @Override
    public String columnNotMatchRegex() {
        return " !regexp(${column}, ${regex}) ";
    }

    @Override
    public String dailyAvg(String uniqueKey) {
        return "SELECT ROUND(AVG(actual_value), 2) AS expected_value_" + uniqueKey +
                " FROM md_dv_actual_values" +
                " WHERE data_time >= TIMESTAMP ${data_time}" +
                " AND data_time < TIMESTAMP ${data_time} + INTERVAL '1' DAY" +
                " AND unique_code = ${unique_code}";
    }

    @Override
    public String last7DayAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >= TIMESTAMP ${data_time} + INTERVAL '-7' DAY" +
                " and data_time < TIMESTAMP ${data_time} + INTERVAL '1' DAY and unique_code = ${unique_code}";
    }

    @Override
    public String last30DayAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >= TIMESTAMP ${data_time} + INTERVAL '-30' DAY" +
                " and data_time < TIMESTAMP ${data_time} + INTERVAL '1' DAY and unique_code = ${unique_code}";
    }

    @Override
    public String monthlyAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >= DATE_TRUNC('MONTH', TIMESTAMP ${data_time})" +
                " and data_time < DATE_TRUNC('MONTH', TIMESTAMP ${data_time}) + INTERVAL '1' MONTH - INTERVAL '1' DAY and unique_code = ${unique_code}";
    }

    @Override
    public String weeklyAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >= DATE_TRUNC('WEEK', TIMESTAMP ${data_time})" +
                " and data_time < DATE_TRUNC('WEEK', TIMESTAMP ${data_time}) + INTERVAL '1' WEEK - INTERVAL '1' DAY and unique_code = ${unique_code}";
    }
}
