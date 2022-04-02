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

import io.datavines.metric.single.table.base.SingleTableBase;

import java.util.Map;

import io.datavines.common.config.CheckResult;
import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;

public class NullCount extends SingleTableBase {

    @Override
    public String getName() {
        return "null_count";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.COMPLETENESS;
    }

    @Override
    public MetricType getType() {
        return MetricType.SINGLE_TABLE;
    }

    @Override
    public String getInvalidateItemsSql() {
        return "SELECT * FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '')";
    }

    @Override
    public String getActualValueSql() {
        return "SELECT COUNT(*) AS actual_value FROM ${invalidate_items_table}";
    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return true;
    }

    @Override
    public CheckResult validateConfig(Map<String, String> config) {
        return null;
    }

}
