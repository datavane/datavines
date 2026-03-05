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

import io.datavines.connector.api.MetricScript;

/**
 * MaxCompute specific MetricScript implementation.
 * Overrides methods where MaxCompute SQL syntax differs from standard SQL.
 */
public class MaxComputeMetricScript implements MetricScript {

    @Override
    public String histogramActualValue(String uniqueKey, String where) {
        return "select concat(k, '\001', cast(count as string)) as actual_value_" + uniqueKey
                + " from (select if(${column} is null, 'NULL', cast(${column} as string)) as k, count(1) as count from ${table} "
                + where + " group by ${column} order by count desc limit 50) T";
    }

    @Override
    public String columnNotMatchRegex() {
        return " not (${column} rlike '${regexp}') ";
    }

    @Override
    public String columnMatchRegex() {
        return " (${column} rlike '${regexp}') ";
    }

    @Override
    public String timeBetweenWithFormat() {
        return " (to_char(${column}, '${datetime_format}') <= to_char(${deadline_time}, '${datetime_format}')) AND (to_char(${column}, '${datetime_format}') >= to_char(${begin_time}, '${datetime_format}')) ";
    }

    @Override
    public String dailyAvg(String uniqueKey) {
        return "select round(avg(actual_value), 2) as expected_value_" + uniqueKey
                + " from dv_actual_values where data_time >= to_char(${data_time}, 'yyyy-MM-dd')"
                + " and data_time < dateadd(to_date(to_char(${data_time}, 'yyyy-MM-dd'), 'yyyy-MM-dd'), 1, 'dd')"
                + " and unique_code = ${unique_code}";
    }

    @Override
    public String last7DayAvg(String uniqueKey) {
        return "select round(avg(actual_value), 2) as expected_value_" + uniqueKey
                + " from dv_actual_values where data_time >= dateadd(to_date(to_char(${data_time}, 'yyyy-MM-dd'), 'yyyy-MM-dd'), -7, 'dd')"
                + " and data_time < dateadd(to_date(to_char(${data_time}, 'yyyy-MM-dd'), 'yyyy-MM-dd'), 1, 'dd')"
                + " and unique_code = ${unique_code}";
    }

    @Override
    public String last30DayAvg(String uniqueKey) {
        return "select round(avg(actual_value), 2) as expected_value_" + uniqueKey
                + " from dv_actual_values where data_time >= dateadd(to_date(to_char(${data_time}, 'yyyy-MM-dd'), 'yyyy-MM-dd'), -30, 'dd')"
                + " and data_time < dateadd(to_date(to_char(${data_time}, 'yyyy-MM-dd'), 'yyyy-MM-dd'), 1, 'dd')"
                + " and unique_code = ${unique_code}";
    }

    @Override
    public String monthlyAvg(String uniqueKey) {
        return "select round(avg(actual_value), 2) as expected_value_" + uniqueKey
                + " from dv_actual_values where data_time >= to_char(${data_time}, 'yyyy-MM-01')"
                + " and data_time < dateadd(to_date(to_char(${data_time}, 'yyyy-MM-dd'), 'yyyy-MM-dd'), 1, 'dd')"
                + " and unique_code = ${unique_code}";
    }

    @Override
    public String weeklyAvg(String uniqueKey) {
        return "select round(avg(actual_value), 2) as expected_value_" + uniqueKey
                + " from dv_actual_values where data_time >= dateadd(${data_time}, -weekday(${data_time}), 'dd')"
                + " and data_time < dateadd(to_date(to_char(${data_time}, 'yyyy-MM-dd'), 'yyyy-MM-dd'), 1, 'dd')"
                + " and unique_code = ${unique_code}";
    }
}
