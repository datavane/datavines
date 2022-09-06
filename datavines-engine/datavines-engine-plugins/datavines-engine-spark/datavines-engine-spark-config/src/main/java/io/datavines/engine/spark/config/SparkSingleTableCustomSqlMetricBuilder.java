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
package io.datavines.engine.spark.config;

import io.datavines.common.config.SinkConfig;
import io.datavines.common.config.TransformConfig;
import io.datavines.common.entity.ExecuteSql;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.engine.config.MetricParserUtils;
import io.datavines.metric.api.ExpectedValue;

import io.datavines.spi.PluginLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.datavines.engine.api.ConfigConstants.*;

public class SparkSingleTableCustomSqlMetricBuilder extends BaseSparkConfigurationBuilder {

    @Override
    public void buildTransformConfigs() {
        List<TransformConfig> transformConfigs = new ArrayList<>();
        List<ExecuteSql> transformExecuteSqlList = new ArrayList<>();
        //get custom execute sql
        transformExecuteSqlList.add(getCustomExecuteSql(inputParameter));
        //get expected value sql
        String expectedType = taskInfo.getEngineType() + "_" + taskParameter.getExpectedType();
        expectedValue = PluginLoader
                .getPluginLoader(ExpectedValue.class)
                .getNewPlugin(expectedType);
        ExecuteSql expectedValueExecuteSql =
                new ExecuteSql(expectedValue.getExecuteSql(),expectedValue.getOutputTable());
        transformExecuteSqlList.add(expectedValueExecuteSql);

        inputParameter.put(EXPECTED_TABLE, expectedValueExecuteSql.getResultTable());
        inputParameter.put(EXPECTED_NAME, expectedValue.getName());

        MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs, transformExecuteSqlList);
        configuration.setTransformParameters(transformConfigs);
    }

    @Override
    public void buildSinkConfigs() throws DataVinesException {
        List<SinkConfig> sinkConfigs = new ArrayList<>();
        //get the actual value storage parameter
        SinkConfig actualValueSinkConfig = getDefaultSinkConfig(SparkSinkSqlBuilder.getActualValueSql(),  "dv_actual_values");
        sinkConfigs.add(actualValueSinkConfig);

        String taskSinkSql = SparkSinkSqlBuilder.getSingleTableCustomSqlSinkSql();
        if (StringUtils.isEmpty(expectedValue.getOutputTable())) {
            taskSinkSql = taskSinkSql.replaceAll("join \\$\\{expected_table}","");
        }
        //get the task data storage parameter
        SinkConfig taskResultSinkConfig = getDefaultSinkConfig(taskSinkSql, "dv_task_result");
        sinkConfigs.add(taskResultSinkConfig);

        //todo
        //get the error data storage parameter
        //support file(hdfs/minio/s3)/es
        SinkConfig errorDataSinkConfig = getErrorSinkConfig();
        sinkConfigs.add(errorDataSinkConfig);

        configuration.setSinkParameters(sinkConfigs);
    }

    private ExecuteSql getCustomExecuteSql(Map<String, String> inputParameterValueResult) {
        inputParameterValueResult.put(ACTUAL_TABLE, inputParameterValueResult.get(TABLE));
        return new ExecuteSql(inputParameterValueResult.get(EXPECTED_EXECUTE_SQL), inputParameterValueResult.get(TABLE));
    }
}
