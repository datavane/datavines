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
package io.datavines.metric.plugin.base;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.ConfigChecker;
import io.datavines.common.entity.ExecuteSql;
import io.datavines.common.utils.JSONUtils;
import io.datavines.metric.api.SqlMetric;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class BaseSingleTable implements SqlMetric {

    protected final StringBuilder invalidateItemsSql = new StringBuilder("select * from ${table}");

    private final StringBuilder actualValueSql = new StringBuilder("select count(1) as actual_value from ${invalidate_items_table}");

    protected List<String> filters = new ArrayList<>();

    protected Set<String> configSet = new HashSet<>();

    protected Set<String> requiredOptions = new HashSet<>();

    public BaseSingleTable() {
        configSet.add("table");
        configSet.add("filter");

        requiredOptions.add("table");
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

    @Override
    public CheckResult validateConfig(Map<String, Object> config) {
        return ConfigChecker.checkConfig(config, requiredOptions);
    }

    @Override
    public void prepare(Map<String, String> config) {
        if (config.containsKey("filter")) {
            filters.add(config.get("filter"));
        }

        addFiltersIntoInvalidateItemsSql();
    }

    private void addFiltersIntoInvalidateItemsSql() {
        if (filters.size() > 0) {
            invalidateItemsSql.append(" where ").append(String.join(" and ", filters));
        }
    }
}