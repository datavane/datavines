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
import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.config.DataQualityConfigurationBuilder;
import io.datavines.engine.config.MetricParserUtils;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.datavines.engine.config.ConfigConstants.*;

public abstract class BaseSparkConfigurationBuilder implements DataQualityConfigurationBuilder {

    protected final DataVinesQualityConfig configuration = new DataVinesQualityConfig();

    protected Map<String, String> inputParameter;

    protected TaskParameter taskParameter;

    protected TaskInfo taskInfo;

    protected ExpectedValue expectedValue;

    private ConnectionInfo connectionInfo;

    @Override
    public void init(Map<String, String> inputParameter, TaskInfo taskInfo, ConnectionInfo connectionInfo) {
        this.inputParameter = inputParameter;
        this.taskInfo = taskInfo;
        this.taskParameter = taskInfo.getTaskParameter();
        this.connectionInfo = connectionInfo;

        if (taskParameter.getMetricParameter() != null) {
            taskParameter.getMetricParameter().forEach((k, v) -> {
                inputParameter.put(k, String.valueOf(v));
            });
        }

        if (taskParameter.getExpectedParameter() != null) {
            taskParameter.getExpectedParameter().forEach((k, v) -> {
                inputParameter.put(k, String.valueOf(v));
            });
        }

        inputParameter.put("result_formula", String.valueOf(taskParameter.getResultFormula()));
        inputParameter.put("operator", String.valueOf(taskParameter.getOperator()));
        inputParameter.put("threshold", String.valueOf(taskParameter.getThreshold()));
        inputParameter.put("failure_strategy", String.valueOf(taskParameter.getFailureStrategy()));

        inputParameter.put(EXPECTED_TYPE, StringUtils.wrapperSingleQuotes(taskParameter.getExpectedType()));
    }

    @Override
    public void buildName() {
        configuration.setName(taskInfo.getName());
    }

    @Override
    public void buildEnvConfig() {
        configuration.setEnvConfig(getEnvConfig());
    }

    @Override
    public void buildSourceConfigs() throws DataVinesException {
        configuration.setSourceParameters(getSourceConfigs());
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

        // get expected value transform sql
        String expectedType = taskParameter.getExpectedType();
        expectedValue = PluginLoader
                .getPluginLoader(ExpectedValue.class)
                .getNewPlugin(expectedType);
        ExecuteSql expectedValueExecuteSql =
                new ExecuteSql(expectedValue.getExecuteSql(),expectedValue.getOutputTable());
        transformExecuteSqlList.add(expectedValueExecuteSql);

        if (StringUtils.isNotEmpty(expectedValueExecuteSql.getResultTable())) {
            inputParameter.put(EXPECTED_TABLE, expectedValueExecuteSql.getResultTable());
        }

        if (StringUtils.isNotEmpty(expectedValue.getName())) {
            inputParameter.put(EXPECTED_NAME, expectedValue.getName());
        }

        MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs, transformExecuteSqlList);
        configuration.setTransformParameters(transformConfigs);
    }

    @Override
    public DataVinesQualityConfig build() {
        return configuration;
    }

    private EnvConfig getEnvConfig() {
        EnvConfig envConfig = new EnvConfig();
        envConfig.setEngine(taskInfo.getEngineType());
        return envConfig;
    }

    protected List<SourceConfig> getSourceConfigs() throws DataVinesException {
        List<SourceConfig> sourceConfigs = new ArrayList<>();

        if (taskParameter.getSrcConnectorParameter() != null) {
            ConnectorParameter srcConnectorParameter = taskParameter.getSrcConnectorParameter();
            SourceConfig sourceConfig = new SourceConfig();
            sourceConfig.setPlugin(srcConnectorParameter.getType());

            Map<String, Object> connectorParameterMap = new HashMap<>(srcConnectorParameter.getParameters());
            connectorParameterMap.putAll(inputParameter);

            ConnectorFactory connectorFactory = PluginLoader
                    .getPluginLoader(ConnectorFactory.class)
                    .getNewPlugin(srcConnectorParameter.getType());

            connectorParameterMap = connectorFactory.getConnectorParameterConverter().converter(connectorParameterMap);

            String outputTable = srcConnectorParameter.getParameters().get(DATABASE) + "_" + inputParameter.get(SRC_TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            inputParameter.put(SRC_TABLE, outputTable);

            sourceConfig.setConfig(connectorParameterMap);
            sourceConfigs.add(sourceConfig);
        }

        if (taskParameter.getTargetConnectorParameter() != null && taskParameter.getTargetConnectorParameter().getParameters() !=null) {
            ConnectorParameter targetConnectorParameter = taskParameter.getTargetConnectorParameter();
            SourceConfig sourceConfig = new SourceConfig();
            sourceConfig.setPlugin(targetConnectorParameter.getType());

            Map<String, Object> connectorParameterMap = new HashMap<>(targetConnectorParameter.getParameters());
            connectorParameterMap.putAll(inputParameter);

            ConnectorFactory connectorFactory = PluginLoader
                    .getPluginLoader(ConnectorFactory.class)
                    .getNewPlugin(targetConnectorParameter.getType());

            connectorParameterMap = connectorFactory.getConnectorParameterConverter().converter(connectorParameterMap);

            String outputTable = targetConnectorParameter.getParameters().get(DATABASE) + "_" + inputParameter.get(TARGET_TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            inputParameter.put(TARGET_TABLE, outputTable);
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

    protected SinkConfig getDefaultSinkConfig(String sql, String dbTable) throws DataVinesException {

        SinkConfig actualValueSinkConfig = new SinkConfig();
        if (connectionInfo == null) {
            throw new DataVinesException("can not get the default datasource info");
        }
        actualValueSinkConfig.setPlugin(connectionInfo.getDriverName());

        actualValueSinkConfig.setConfig(
                getDefaultSourceConfigMap(
                        PlaceholderUtils.replacePlaceholders(
                                sql, inputParameter,true),dbTable));
        return actualValueSinkConfig;
    }

    protected SourceConfig getDefaultSourceConfig() throws DataVinesException {

        SourceConfig actualValueSourceConfig = new SourceConfig();
        if (connectionInfo == null) {
            throw new DataVinesException("can not get the default datasource info");
        }
        actualValueSourceConfig.setPlugin(connectionInfo.getDriverName());

        actualValueSourceConfig.setConfig(getDefaultSourceConfigMap(null,null));
        return actualValueSourceConfig;
    }

    protected Map<String,Object> getDefaultSourceConfigMap(String sql, String dbTable) {
        Map<String,Object> actualValueConfigMap = new HashMap<>();
        actualValueConfigMap.put("url", connectionInfo.getUrl());
        actualValueConfigMap.put("dbtable", dbTable);
        actualValueConfigMap.put("user", connectionInfo.getUsername());
        actualValueConfigMap.put("password", connectionInfo.getPassword());
        if (StringUtils.isNotEmpty(sql)) {
            actualValueConfigMap.put(SQL, sql);
        }

        return actualValueConfigMap;
    }

}
