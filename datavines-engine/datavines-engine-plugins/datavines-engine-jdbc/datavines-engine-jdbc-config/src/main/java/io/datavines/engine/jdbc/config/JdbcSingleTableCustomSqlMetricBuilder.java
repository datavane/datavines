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

package io.datavines.engine.jdbc.config;

import io.datavines.common.config.TransformConfig;
import io.datavines.common.config.enums.TransformType;
import io.datavines.common.entity.ExecuteSql;
import io.datavines.common.utils.StringUtils;
import io.datavines.engine.config.MetricParserUtils;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.datavines.engine.config.ConfigConstants.*;

public class JdbcSingleTableCustomSqlMetricBuilder extends JdbcSingleTableMetricBuilder {

    @Override
    public void buildTransformConfigs() {

        String metricType = taskParameter.getMetricType();
        SqlMetric sqlMetric = PluginLoader
                .getPluginLoader(SqlMetric.class)
                .getNewPlugin(metricType);

        MetricParserUtils.operateInputParameter(inputParameter, sqlMetric, taskInfo);

        List<TransformConfig> transformConfigs = new ArrayList<>();
        //get custom execute sql
        MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs,
                getCustomExecuteSql(inputParameter), TransformType.ACTUAL_VALUE.getDescription());

        // get expected value transform sql
        String expectedType = taskParameter.getExpectedType();
        expectedValue = PluginLoader
                .getPluginLoader(ExpectedValue.class)
                .getNewPlugin(expectedType);

        ExecuteSql expectedValueExecuteSql =
                new ExecuteSql(expectedValue.getExecuteSql(),expectedValue.getOutputTable());

        if (StringUtils.isNotEmpty(expectedValueExecuteSql.getResultTable())) {
            inputParameter.put(EXPECTED_TABLE, expectedValueExecuteSql.getResultTable());
        }

        if (expectedValue.isNeedDefaultDatasource()) {
            MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs,
                    expectedValueExecuteSql, TransformType.EXPECTED_VALUE_FROM_METADATA_SOURCE.getDescription());
        } else {
            MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs,
                    expectedValueExecuteSql, TransformType.EXPECTED_VALUE_FROM_SOURCE.getDescription());
        }
        configuration.setTransformParameters(transformConfigs);
    }

    private ExecuteSql getCustomExecuteSql(Map<String, String> inputParameterValueResult) {
        inputParameterValueResult.put(ACTUAL_TABLE, inputParameterValueResult.get(TABLE));
        return new ExecuteSql(inputParameterValueResult.get(ACTUAL_EXECUTE_SQL), inputParameterValueResult.get(TABLE));
    }
}
