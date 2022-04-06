package io.datavines.engine.jdbc.config;

import io.datavines.common.config.*;
import io.datavines.common.config.enums.SourceType;
import io.datavines.common.config.enums.TransformType;
import io.datavines.common.entity.*;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.config.ConfigConstants;
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

public abstract class BaseJdbcConfigurationBuilder implements DataQualityConfigurationBuilder {

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

        inputParameter.put(INVALIDATE_ITEMS_TABLE,
                sqlMetric.getInvalidateItems().getResultTable()
                        + "_" + inputParameter.get(ConfigConstants.TASK_ID));

        MetricParserUtils.setTransformerConfig(
                inputParameter, transformConfigs,
                sqlMetric.getInvalidateItems(), TransformType.INVALIDATE_ITEMS.getDescription());

        MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs,
                sqlMetric.getActualValue(), TransformType.ACTUAL_VALUE.getDescription());

        inputParameter.put(ACTUAL_TABLE, sqlMetric.getActualValue().getResultTable());

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

        if (StringUtils.isNotEmpty(expectedValue.getName())) {
            inputParameter.put(EXPECTED_NAME, expectedValue.getName());
        }

        if (expectedValue.isNeedDefaultDatasource()) {
            MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs,
                    expectedValueExecuteSql, TransformType.EXPECTED_VALUE_FROM_METADATA_SOURCE.getDescription());
        } else {
            MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs,
                    expectedValueExecuteSql, TransformType.EXPECTED_VALUE_FROM_SRC_SOURCE.getDescription());
        }

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

            Map<String, Object> connectorParameterMap = new HashMap<>(srcConnectorParameter.getParameters());
            connectorParameterMap.putAll(inputParameter);

            ConnectorFactory connectorFactory = PluginLoader
                    .getPluginLoader(ConnectorFactory.class)
                    .getNewPlugin(srcConnectorParameter.getType());

            connectorParameterMap = connectorFactory.getConnectorParameterConverter().converter(connectorParameterMap);

            String outputTable = inputParameter.get(SRC_TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            connectorParameterMap.put(DRIVER, connectorFactory.getDialect().getDriver());

            sourceConfig.setPlugin(connectorFactory.getCategory());
            sourceConfig.setConfig(connectorParameterMap);
            sourceConfig.setType(SourceType.NORMAL.getDescription());
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

            String outputTable = inputParameter.get(TARGET_TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            connectorParameterMap.put(DRIVER, connectorFactory.getDialect().getDriver());

            sourceConfig.setPlugin(connectorFactory.getCategory());
            sourceConfig.setConfig(connectorParameterMap);
            sourceConfig.setType(SourceType.NORMAL.getDescription());
            sourceConfigs.add(sourceConfig);
        }

        String expectedType = taskParameter.getExpectedType();
        if (StringUtils.isEmpty(expectedType)) {
            return sourceConfigs;
        }

        expectedValue = PluginLoader
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
        actualValueSinkConfig.setPlugin("jdbc");
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
        actualValueSourceConfig.setPlugin("jdbc");
        actualValueSourceConfig.setType(SourceType.METADATA.getDescription());
        actualValueSourceConfig.setConfig(getDefaultSourceConfigMap(null,null));
        return actualValueSourceConfig;
    }

    protected Map<String,Object> getDefaultSourceConfigMap(String sql, String dbTable) {
        Map<String,Object> actualValueConfigMap = new HashMap<>();
        actualValueConfigMap.put(URL, connectionInfo.getUrl());
        actualValueConfigMap.put(DB_TABLE, dbTable);
        actualValueConfigMap.put(USER, connectionInfo.getUsername());
        actualValueConfigMap.put(PASSWORD, connectionInfo.getPassword());
        actualValueConfigMap.put(DRIVER, connectionInfo.getDriverName());
        if (StringUtils.isNotEmpty(sql)) {
            actualValueConfigMap.put(SQL, sql);
        }

        return actualValueConfigMap;
    }

}
