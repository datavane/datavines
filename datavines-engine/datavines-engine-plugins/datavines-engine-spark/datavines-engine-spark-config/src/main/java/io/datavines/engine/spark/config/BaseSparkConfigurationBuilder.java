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

import io.datavines.common.config.*;
import io.datavines.common.entity.*;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.config.BaseDataQualityConfigurationBuilder;
import io.datavines.engine.config.MetricParserUtils;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.datavines.engine.api.ConfigConstants.*;

public abstract class BaseSparkConfigurationBuilder extends BaseDataQualityConfigurationBuilder {

    @Override
    protected EnvConfig getEnvConfig() {
        EnvConfig envConfig = new EnvConfig();
        envConfig.setEngine(taskInfo.getEngineType());
        return envConfig;
    }

    @Override
    protected List<SourceConfig> getSourceConfigs() throws DataVinesException {
        List<SourceConfig> sourceConfigs = new ArrayList<>();

        if (taskParameter.getSrcConnectorParameter() != null) {
            ConnectorParameter srcConnectorParameter = taskParameter.getSrcConnectorParameter();
            SourceConfig sourceConfig = new SourceConfig();

            Map<String, Object> connectorParameterMap = new HashMap<>(srcConnectorParameter.getParameters());
            connectorParameterMap.putAll(inputParameter);

            ConnectorFactory connectorFactory = PluginLoader
                    .getPluginLoader(ConnectorFactory.class)
                    .getNewPlugin(srcConnectorParameter.getType());

            connectorParameterMap = connectorFactory.getConnectorParameterConverter().converter(connectorParameterMap);

            String outputTable = srcConnectorParameter.getParameters().get(DATABASE) + "_" + inputParameter.get(TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            connectorParameterMap.put(DRIVER, connectorFactory.getDialect().getDriver());
            inputParameter.put(TABLE, outputTable);

            sourceConfig.setPlugin(connectorFactory.getCategory());
            sourceConfig.setConfig(connectorParameterMap);
            sourceConfigs.add(sourceConfig);
        }

        if (taskParameter.getTargetConnectorParameter() != null && taskParameter.getTargetConnectorParameter().getParameters() !=null) {
            ConnectorParameter targetConnectorParameter = taskParameter.getTargetConnectorParameter();
            SourceConfig sourceConfig = new SourceConfig();

            Map<String, Object> connectorParameterMap = new HashMap<>(targetConnectorParameter.getParameters());
            connectorParameterMap.putAll(inputParameter);

            ConnectorFactory connectorFactory = PluginLoader
                    .getPluginLoader(ConnectorFactory.class)
                    .getNewPlugin(targetConnectorParameter.getType());

            connectorParameterMap = connectorFactory.getConnectorParameterConverter().converter(connectorParameterMap);

            String outputTable = targetConnectorParameter.getParameters().get(DATABASE) + "_" + inputParameter.get(TARGET_TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            connectorParameterMap.put(DRIVER, connectorFactory.getDialect().getDriver());
            inputParameter.put(TARGET_TABLE, outputTable);

            sourceConfig.setPlugin(connectorFactory.getCategory());
            sourceConfig.setConfig(connectorParameterMap);
            sourceConfigs.add(sourceConfig);
        }

        String expectedType = taskParameter.getExpectedType();
        if (StringUtils.isEmpty(expectedType)) {
            return sourceConfigs;
        }

        ExpectedValue expectedValue = PluginLoader
                .getPluginLoader(ExpectedValue.class)
                .getNewPlugin(expectedType);
        if (expectedValue.isNeedDefaultDatasource()) {
            sourceConfigs.add(getDefaultSourceConfig());
        }

        return sourceConfigs;
    }

    @Override
    public void buildTransformConfigs() {
        String metricType = taskParameter.getMetricType();
        SqlMetric sqlMetric = PluginLoader
                .getPluginLoader(SqlMetric.class)
                .getNewPlugin(metricType);

        MetricParserUtils.operateInputParameter(inputParameter, sqlMetric, taskInfo);

        List<TransformConfig> transformConfigs = new ArrayList<>();
        List<ExecuteSql> transformExecuteSqlList = new ArrayList<>();
        transformExecuteSqlList.add(sqlMetric.getInvalidateItems());
        transformExecuteSqlList.add(sqlMetric.getActualValue());
        inputParameter.put(ACTUAL_TABLE, sqlMetric.getActualValue().getResultTable());
        inputParameter.put(ACTUAL_VALUE, sqlMetric.getActualName());
        inputParameter.put(INVALIDATE_ITEMS_TABLE, sqlMetric.getInvalidateItems().getResultTable());

        // get expected value transform sql
        String expectedType = taskParameter.getExpectedType();
        expectedValue = PluginLoader
                .getPluginLoader(ExpectedValue.class)
                .getNewPlugin(expectedType);
        ExecuteSql expectedValueExecuteSql =
                new ExecuteSql(expectedValue.getExecuteSql(), expectedValue.getOutputTable());
        transformExecuteSqlList.add(expectedValueExecuteSql);

        if (StringUtils.isNotEmpty(expectedValueExecuteSql.getResultTable())) {
            inputParameter.put(EXPECTED_TABLE, expectedValueExecuteSql.getResultTable());
        }

        if (StringUtils.isNotEmpty(expectedValue.getName())) {
            inputParameter.put(EXPECTED_VALUE, expectedValue.getName());
        }

        MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs, transformExecuteSqlList);
        configuration.setTransformParameters(transformConfigs);
    }
}
