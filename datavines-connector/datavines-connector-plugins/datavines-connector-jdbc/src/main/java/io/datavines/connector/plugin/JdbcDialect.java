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
package io.datavines.connector.plugin;

import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.Dialect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.datavines.common.ConfigConstants.*;

public abstract class JdbcDialect implements Dialect {

    protected final HashMap<String,String> dialectKeyMap = new HashMap<>();

    @Override
    public Map<String, String> getDialectKeyMap() {
        dialectKeyMap.put(REGEX_KEY, "${column} regexp '${regexp}'");
        dialectKeyMap.put(NOT_REGEX_KEY, "${column} not regexp '${regexp}'");
        dialectKeyMap.put(STRING_TYPE, "varchar");
        dialectKeyMap.put(IF_FUNCTION_KEY, "if");
        dialectKeyMap.put(LIMIT_TOP_50_KEY, " limit 50");
        dialectKeyMap.put(LENGTH_KEY, "length(${column})");
        dialectKeyMap.put(IF_CASE_KEY, "if(${column} is null, 'NULL', cast(${column} as ${string_type}))");
        dialectKeyMap.put(STD_DEV_KEY, "stddev");
        dialectKeyMap.put(VARIANCE_KEY, "variance");
        return dialectKeyMap;
    }

    @Override
    public String getColumnPrefix() {
        return "`";
    }

    @Override
    public String getColumnSuffix() {
        return "`";
    }

    @Override
    public List<String> getExcludeDatabases() {
        return Arrays.asList("sys", "information_schema", "performance_schema", "mysql");
    }

    @Override
    public String getErrorDataScript(Map<String, String> configMap) {
        String errorDataFileName = configMap.get("error_data_file_name");
        if (StringUtils.isNotEmpty(errorDataFileName)) {
            if (StringUtils.isEmpty(configMap.get(ERROR_DATA_OUTPUT_TO_DATASOURCE_DATABASE))) {
                return "select * from " + errorDataFileName;
            }
            return "select * from " + configMap.get(ERROR_DATA_OUTPUT_TO_DATASOURCE_DATABASE) + "." + errorDataFileName;
        }
        return null;
    }

    @Override
    public String getValidateResultDataScript(Map<String, String> configMap) {
        String executionId = configMap.get("execution_id");
        if (StringUtils.isNotEmpty(executionId)) {
            return "select * from dv_job_execution_result where job_execution_id = " + executionId;
        }
        return null;
    }

}
