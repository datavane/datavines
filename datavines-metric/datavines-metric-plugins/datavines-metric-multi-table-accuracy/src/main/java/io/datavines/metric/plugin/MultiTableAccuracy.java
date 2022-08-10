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
package io.datavines.metric.plugin;

import java.util.*;

import io.datavines.common.config.CheckResult;
import io.datavines.common.entity.ExecuteSql;
import io.datavines.metric.api.ConfigItem;
import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.api.SqlMetric;

public class MultiTableAccuracy implements SqlMetric {

    private StringBuilder sourceTableSql = new StringBuilder("SELECT * FROM ${table})");

    private StringBuilder targetTableSql = new StringBuilder("SELECT * FROM ${table2})");

    private StringBuilder invalidateItemsSql = new StringBuilder("SELECT ${table}.* FROM (");

    private StringBuilder actualValueSql = new StringBuilder("select count(1) as actual_value from ${invalidate_items_table}");

    @Override
    public String getName() {
        return "MultiTableAccuracy";
    }

    @Override
    public String getZhName() {
        return "跨表准确性检查";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.ACCURACY;
    }

    @Override
    public MetricType getType() {
        return MetricType.MULTI_TABLE_ACCURACY;
    }

    @Override
    public String getActualName() {
        return null;
    }

    @Override
    public String getIssue() {
        return null;
    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return true;
    }

    @Override
    public CheckResult validateConfig(Map<String, Object> config) {
        return null;
    }

    @Override
    public void prepare(Map<String, String> config) {
        if (config.containsKey("fliter")) {
            sourceTableSql.append("WHERE (${filter})");
        }

        if (config.containsKey("fliter2")) {
            targetTableSql.append("WHERE (${filter2})");
        }

        invalidateItemsSql
                .append("(").append(sourceTableSql.toString()).append(")").append(" ${table} ")
                .append(" LEFT JOIN ")
                .append("(").append(targetTableSql.toString()).append(")").append(" ${table2} ")
                .append("ON ${on_clause} WHERE ${where_clause}");
    }

    @Override
    public Map<String, ConfigItem> getConfigMap() {
        return new HashMap<>();
    }

    @Override
    public ExecuteSql getInvalidateItems() {
        ExecuteSql executeSql = new ExecuteSql();
        executeSql.setResultTable("invalidate_items");
        executeSql.setSql(invalidateItemsSql.toString());
        executeSql.setErrorOutput(isInvalidateItemsCanOutput());
        return executeSql;
    }

    @Override
    public ExecuteSql getActualValue() {
        ExecuteSql executeSql = new ExecuteSql();
        executeSql.setResultTable("invalidate_count");
        executeSql.setSql(actualValueSql.toString());
        executeSql.setErrorOutput(false);
        return executeSql;
    }
}
