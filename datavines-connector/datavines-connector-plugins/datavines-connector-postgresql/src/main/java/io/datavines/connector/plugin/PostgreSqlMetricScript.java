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

public class PostgreSqlMetricScript extends JdbcMetricScript {

    @Override
    public String maxLengthActualValue(String uniqueKey) {
        return "select max(length(${column}::text)) as actual_value_"+ uniqueKey +" from ${table}";
    }

    @Override
    public String minLengthActualValue(String uniqueKey) {
        return "select min(length(${column}::text)) as actual_value_"+ uniqueKey +" from ${table}";
    }

    @Override
    public String columnLengthCompare() {
        return " length(${column}::text) ${comparator} ${length} ";
    }

    @Override
    public String columnMatchRegex() {
        return " ${column} ~ '${regexp}' ";
    }

    @Override
    public String columnNotMatchRegex() {
        return " ${column} !~ '${regexp}' ";
    }

    @Override
    public String avgLengthActualValue(String uniqueKey) {
        return "select avg(length(${column}::text)) as actual_value_"+ uniqueKey +" from ${table}";
    }

    @Override
    public String histogramActualValue(String uniqueKey, String where) {
        return "select concat(k, '\001', cast(count as varchar)) as actual_value_" +
                uniqueKey + " from (select case when ${column} is null then 'NULL' else cast(${column} as varchar) end as k, count(1) as count from ${table} " +
                where + " group by ${column} order by count desc limit 50) T";
    }
}
