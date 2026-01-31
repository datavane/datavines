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
import io.datavines.common.config.ConfigChecker;
import io.datavines.common.entity.ExecuteSql;
import io.datavines.common.enums.DataVinesDataType;
import io.datavines.common.utils.StringUtils;
import io.datavines.metric.api.ConfigItem;
import io.datavines.metric.api.MetricDimension;
import io.datavines.metric.api.MetricType;
import io.datavines.metric.api.SqlMetric;

import static io.datavines.common.CommonConstants.TABLE;
import static io.datavines.common.ConfigConstants.*;

public class CustomCountSql implements SqlMetric {

    private final Set<String> requiredOptions = new HashSet<>();

    private final HashMap<String, ConfigItem> configMap = new HashMap<>();

    private String invalidateItemsSql = null;

    public CustomCountSql() {
        configMap.put(INVALIDATE_ITEMS_SQL, new ConfigItem(INVALIDATE_ITEMS_SQL, "错误数据SQL", INVALIDATE_ITEMS_SQL));

        requiredOptions.add(INVALIDATE_ITEMS_SQL);
    }

    @Override
    public String getName() {
        return "custom_count_sql";
    }

    @Override
    public String getZhName() {
        return "自定义统计SQL";
    }

    @Override
    public MetricDimension getDimension() {
        return MetricDimension.ACCURACY;
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
    public CheckResult validateConfig(Map<String, Object> config) {
        CheckResult basicCheck = ConfigChecker.checkConfig(config, requiredOptions);
        if (!basicCheck.isSuccess()) {
            return basicCheck;
        }

        Object sqlValue = config.get(INVALIDATE_ITEMS_SQL);
        if (sqlValue == null || StringUtils.isEmpty(String.valueOf(sqlValue).trim())) {
            return new CheckResult(false, INVALIDATE_ITEMS_SQL + " cannot be empty");
        }

        return new CheckResult(true, "");
    }

    @Override
    public void prepare(Map<String, String> config) {
        if (config.containsKey(INVALIDATE_ITEMS_SQL) && StringUtils.isNotEmpty(config.get(INVALIDATE_ITEMS_SQL))) {
            this.invalidateItemsSql = config.get(INVALIDATE_ITEMS_SQL);
        }
    }

    @Override
    public Map<String, ConfigItem> getConfigMap() {
        return configMap;
    }

    @Override
    public ExecuteSql getInvalidateItems(Map<String, String> inputParameter) {
        if (StringUtils.isEmpty(invalidateItemsSql)) {
            throw new IllegalStateException("invalidate_items_sql is not configured or empty");
        }

        String uniqueKey = inputParameter.get(METRIC_UNIQUE_KEY);
        if (StringUtils.isEmpty(uniqueKey)) {
            throw new IllegalStateException("metric_unique_key is missing in input parameters");
        }

        ExecuteSql executeSql = new ExecuteSql();
        executeSql.setResultTable("invalidate_items_" + uniqueKey);
        executeSql.setSql(invalidateItemsSql);
        executeSql.setErrorOutput(true);
        return executeSql;
    }

    @Override
    public ExecuteSql getActualValue(Map<String, String> inputParameter) {
        if (StringUtils.isEmpty(invalidateItemsSql)) {
            throw new IllegalStateException("invalidate_items_sql is not configured or empty");
        }

        String uniqueKey = inputParameter.get(METRIC_UNIQUE_KEY);
        if (StringUtils.isEmpty(uniqueKey)) {
            throw new IllegalStateException("metric_unique_key is missing in input parameters");
        }

        inputParameter.put(ACTUAL_TABLE, inputParameter.get(TABLE));

        // Auto-generate aggregate SQL by wrapping user's error data SQL with COUNT(*)
        String actualAggregateSql = "SELECT COUNT(*) as actual_value_" + uniqueKey
                + " FROM (" + invalidateItemsSql + ") dv_custom_count_tmp";

        return new ExecuteSql(actualAggregateSql, inputParameter.get(TABLE));
    }

    @Override
    public List<DataVinesDataType> suitableType() {
        return Collections.emptyList();
    }

    @Override
    public boolean isCustomSql() {
        return true;
    }
}
