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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.datavines.common.config.CheckResult;
import io.datavines.common.entity.ExecuteSql;
import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.api.SqlMetric;

public class CustomSql implements SqlMetric {

    @Override
    public String getName() {
        return "custom_sql";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.ACCURACY;
    }

    @Override
    public MetricType getType() {
        return MetricType.SINGLE_TABLE_CUSTOM_SQL;
    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return false;
    }

    @Override
    public CheckResult validateConfig(Map<String, Object> config) {
        return new CheckResult(true,"");
    }

    @Override
    public void prepare(Map<String, String> config) {

    }

    @Override
    public Set<String> getConfigSet() {
        return Collections.emptySet();
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
