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

package io.datavines.engine.jdbc.transform.sql;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.common.config.enums.TransformType;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.jdbc.api.JdbcRuntimeEnvironment;
import io.datavines.engine.jdbc.api.JdbcTransform;
import io.datavines.engine.jdbc.api.entity.ResultList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SqlTransform implements JdbcTransform {

    private static final Logger logger = LoggerFactory.getLogger(SqlTransform.class);

    private Config config = new Config();

    @Override
    public void setConfig(Config config) {
        if(config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {
        List<String> requiredOptions = Arrays.asList("sql", "plugin_type");

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x->{
            if(!config.has(x)){
                nonExistsOptions.add(x);
            }
        });

        if (!nonExistsOptions.isEmpty()) {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new CheckResult(true, "");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment env) {

    }

    @Override
    public ResultList process(JdbcRuntimeEnvironment env) {

        ResultList resultList = null;
        try {
            String outputTable = config.getString("invalidate_items_table");
            String sql = config.getString("sql");
            logger.info("transform sql is: {}", sql);
            switch (TransformType.of(config.getString("plugin_type"))){
                case INVALIDATE_ITEMS:
                    resultList = new InvalidateItemsExecutor().execute(env.getSourceConnection(), sql, outputTable);
                    break;
                case ACTUAL_VALUE:
                    resultList = new ActualValueExecutor().execute(env.getSourceConnection(), sql, outputTable);
                    break;
                case EXPECTED_VALUE_FROM_METADATA_SOURCE:
                    resultList = new ExpectedValueExecutor().execute(env.getMetadataConnection(), sql, outputTable);
                    break;
                case EXPECTED_VALUE_FROM_SOURCE:
                    resultList = new ExpectedValueExecutor().execute(env.getSourceConnection(), sql, outputTable);
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            logger.error("transform execute error: ", e);
        }

        return resultList;
    }
}
