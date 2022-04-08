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

import io.datavines.common.entity.ExecuteSql;
import io.datavines.common.utils.JSONUtils;
import io.datavines.metric.api.SqlMetric;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class BaseSingleTable implements SqlMetric {

    private final StringBuilder invalidateItemsSql = new StringBuilder("select * from ${src_table}");

    private final StringBuilder actualValueSql = new StringBuilder("select count(*) as actual_value from ${invalidate_items_table}");

    protected List<String> filters = new ArrayList<>();

    protected static Set<String> configSet = new HashSet<>();

    static {
        configSet.add("src_table");
        configSet.add("src_filter");
        configSet.add("src_column");
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
    public void prepare(Map<String, String> config) {
        if (config.containsKey("src_filter")) {
            filters.add(config.get("src_filter"));
        }

        addFiltersIntoInvalidateItemsSql();
    }

    protected void addFiltersIntoInvalidateItemsSql() {
        if (filters.size() > 0) {
            invalidateItemsSql.append(" where ").append(String.join(" and ", filters));
        }
    }
}