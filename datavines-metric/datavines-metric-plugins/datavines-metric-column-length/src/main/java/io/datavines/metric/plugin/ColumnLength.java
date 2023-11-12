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

import io.datavines.common.enums.DataVinesDataType;
import io.datavines.metric.api.ConfigItem;
import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.plugin.base.BaseSingleTableColumn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColumnLength extends BaseSingleTableColumn {

    public ColumnLength(){
        super();

        configMap.put("length",new ConfigItem("length", "长度", "length"));
        configMap.put("comparator",new ConfigItem("comparator", "比较符", "comparator"));

        requiredOptions.add("length");
        requiredOptions.add("comparator");
    }

    @Override
    public String getName() {
        return "column_length";
    }

    @Override
    public String getZhName() {
        return "长度检查";
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

        if (config.containsKey("length") && config.containsKey("comparator") && config.containsKey("column")) {
            filters.add(" ${length_key} ${comparator} ${length}");
        }
        super.prepare(config);
    }

    @Override
    public List<DataVinesDataType> suitableType() {
        return Arrays.asList(DataVinesDataType.STRING_TYPE, DataVinesDataType.DATE_TIME_TYPE);
    }
}
