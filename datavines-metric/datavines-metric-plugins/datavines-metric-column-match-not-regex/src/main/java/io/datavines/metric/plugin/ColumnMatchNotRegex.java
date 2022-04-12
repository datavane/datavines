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

import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.plugin.base.BaseSingleTable;

import java.util.Map;
import java.util.Set;

public class ColumnMatchNotRegex extends BaseSingleTable {

    public ColumnMatchNotRegex(){
        configSet.add("regexp");
        configSet.add("column");

        REQUIRED_OPTIONS.add("regexp");
        REQUIRED_OPTIONS.add("column");
    }

    @Override
    public String getName() {
        return "column_regex";
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
    public boolean isInvalidateItemsCanOutput() {
        return true;
    }

    @Override
    public void prepare(Map<String, String> config) {

        if (config.containsKey("table") && config.containsKey("column") && config.containsKey("regexp") ) {
            filters.add(" ${column} ${not_regex_key} '${regexp}'");
        }
        super.prepare(config);
    }

    @Override
    public Set<String> getConfigSet() {
        return configSet;
    }
}
