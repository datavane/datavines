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
package io.datavines.metric.expected.plugin;

import io.datavines.metric.api.ExpectedValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static io.datavines.common.ConfigConstants.METRIC_UNIQUE_KEY;

public class TargetTableTotalRows implements ExpectedValue {

    private final StringBuilder sql =
            new StringBuilder("select count(1) as expected_value from ${target_table}");

    @Override
    public String getName() {
        return "target_table_total_rows";
    }

    @Override
    public String getZhName() {
        return "目标表总行数";
    }

    @Override
    public String getKey(Map<String,String> inputParameter) {
        String uniqueKey = inputParameter.get(METRIC_UNIQUE_KEY);
        return "expected_value_" + uniqueKey;
    }

    @Override
    public String getExecuteSql(Map<String,String> inputParameter) {
        String uniqueKey = inputParameter.get(METRIC_UNIQUE_KEY);
        String newKey = "expected_value_" + uniqueKey;
        return sql.toString().replace("expected_value",newKey);
    }

    @Override
    public String getOutputTable(Map<String,String> inputParameter) {
        String uniqueKey = inputParameter.get(METRIC_UNIQUE_KEY);
        return "target_table_total_count_" + uniqueKey;
    }

    @Override
    public boolean isNeedDefaultDatasource() {
        return false;
    }

    @Override
    public void prepare(Map<String, String> config) {
        if (config.containsKey("filter") && StringUtils.isNotBlank(config.get("filter"))) {
            if (sql.toString().contains("where")) {
                sql.append(" and ").append(config.get("filter"));
            } else {
                sql.append(" where ").append(config.get("filter"));
            }
        }
    }
}
