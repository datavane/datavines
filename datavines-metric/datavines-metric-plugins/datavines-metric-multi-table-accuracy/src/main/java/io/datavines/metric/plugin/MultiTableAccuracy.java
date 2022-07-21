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

//    @Override
//    public String getInvalidateItemsSql() {
//        return "SELECT ${table}.* FROM (SELECT * FROM ${table} WHERE (${filter})) ${table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) ${target_table} ON ${on_clause} WHERE ${where_clause}";
//    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return true;
    }

//    @Override
//    public String getActualValueSql() {
//        return "SELECT COUNT(*) AS invalidate_count from invalidate_items";
//    }

    @Override
    public CheckResult validateConfig(Map<String, Object> config) {
        return null;
    }

    @Override
    public void prepare(Map<String, String> config) {

    }

    @Override
    public Map<String, ConfigItem> getConfigMap() {
        return new HashMap<>();
    }

    @Override
    public ExecuteSql getInvalidateItems() {
        return null;
    }

    @Override
    public ExecuteSql getActualValue() {
        return null;
    }
}
