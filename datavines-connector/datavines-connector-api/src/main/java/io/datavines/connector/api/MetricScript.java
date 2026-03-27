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
package io.datavines.connector.api;

public interface MetricScript {

    default String selectFromTable() {
        return "select * from ${table}";
    }

    default String baseActualValue(String uniqueKey) {
        return "select count(1) as actual_value_"+ uniqueKey +" from ${invalidate_items_table}";
    }

    default String baseDirectActualValue(String uniqueKey, String invalidateItemsSql) {
        return "select count(1) as actual_value_"+ uniqueKey +" from ( " + invalidateItemsSql + " ) t";
    }

    default String avgActualValue(String uniqueKey) {
        return "select avg(${column}) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String avgLengthActualValue(String uniqueKey) {
        return "select avg(length(${column})) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String countDistinctActualValue(String uniqueKey) {
        return "select count(distinct(${column})) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String histogramActualValue(String uniqueKey, String where) {
        return "select concat(k, '\001', cast(count as varchar)) as actual_value_" + uniqueKey + " from (select if(${column} is null, 'NULL', cast(${column} as varchar)) as k, count(1) as count from ${table} " + where + " group by ${column} order by count desc limit 50) T";
    }

    default String maxActualValue(String uniqueKey) {
        return "select max(${column}) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String maxLengthActualValue(String uniqueKey) {
        return "select max(length(${column})) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String minActualValue(String uniqueKey) {
        return "select min(${column}) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String minLengthActualValue(String uniqueKey) {
        return "select min(length(${column})) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String stdDevActualValue(String uniqueKey) {
        return "select stddev(${column}) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String sumActualValue(String uniqueKey) {
        return "select sum(${column}) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String varianceActualValue(String uniqueKey) {
        return "select variance(${column}) as actual_value_"+ uniqueKey +" from ${table}";
    }

    default String groupByHavingCountForUnique() {
        return " group by ${column} having count(1) = 1 ";
    }

    default String groupByHavingCountForDuplicate() {
        return " group by ${column} having count(${column}) > 1 ";
    }

    default String columnInEnums() {
        return " (${column} in ( ${enum_list} )) ";
    }

    default String columnNotInEnums() {
        return " (${column} not in ( ${enum_list} ) or ${column} is null) ";
    }

    default String columnNotNull() {
        return " ${column} is not null ";
    }

    default String columnIsNull() {
        return " (${column} is null) ";
    }

    default String columnLengthCompare() {
        return " length(${column}) ${comparator} ${length} ";
    }

    default String columnNotMatchRegex() {
        return " ${column} not regexp '${regexp}' ";
    }

    default String columnMatchRegex() {
        return " ${column} regexp '${regexp}' ";
    }

    default String columnGteMin() {
        return " ${column} >= ${min} ";
    }

    default String columnLteMax() {
        return " ${column} <= ${max} ";
    }

    default String columnIsBlank() {
        return " (${column} is null or ${column} = '') ";
    }

    default String timeBetweenWithFormat() {
        return "  (DATE_FORMAT(${column}, '${datetime_format}') <= DATE_FORMAT(${deadline_time}, '${datetime_format}') ) AND (DATE_FORMAT(${column}, '${datetime_format}') >= DATE_FORMAT(${begin_time}, '${datetime_format}')) ";
    }

    default String dailyAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >=date_format(${data_time},'%Y-%m-%d')" +
                " and data_time < date_add(date_format(${data_time},'%Y-%m-%d'), interval 1 DAY)" +
                " and unique_code = ${unique_code}";
    }

    default String last7DayAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >= date_sub(date_format(${data_time},'%Y-%m-%d'),interval 7 DAY)" +
                " and data_time < date_add(date_format(${data_time},'%Y-%m-%d'),interval 1 DAY) and unique_code = ${unique_code}";
    }

    default String last30DayAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >= date_sub(date_format(${data_time},'%Y-%m-%d'),interval 30 DAY)" +
                " and data_time < date_add(date_format(${data_time},'%Y-%m-%d'),interval 1 DAY) and unique_code = ${unique_code}";
    }

    default String monthlyAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >= date_format(curdate(), '%Y-%m-01') and data_time < date_add(date_format(${data_time},'%Y-%m-%d'),interval 1 DAY)" +
                " and unique_code = ${unique_code}";
    }

    default String weeklyAvg(String uniqueKey) {
        return "select round(avg(actual_value),2) as expected_value_" + uniqueKey +
                " from dv_actual_values where data_time >= date_sub(${data_time},interval weekday(${data_time}) + 0 day)" +
                " and data_time < date_add(date_format(${data_time},'%Y-%m-%d'),interval 1 DAY) and unique_code = ${unique_code}";
    }
}
