package io.datavines.engine.config;

import io.datavines.common.config.*;
import io.datavines.common.config.enums.SourceType;
import io.datavines.common.config.enums.TransformType;
import io.datavines.common.entity.*;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.datavines.engine.config.ConfigConstants.*;
import static io.datavines.engine.config.ConfigConstants.SQL;

public abstract class BaseDataQualityConfigurationBuilder implements DataQualityConfigurationBuilder {

    protected final DataVinesQualityConfig configuration = new DataVinesQualityConfig();

    protected Map<String, String> inputParameter;

    protected TaskParameter taskParameter;

    protected TaskInfo taskInfo;

    protected ExpectedValue expectedValue;

    protected ConnectionInfo connectionInfo;

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
    public DataVinesQualityConfig build() {
        return configuration;
    }

    protected abstract EnvConfig getEnvConfig();

    protected abstract List<SourceConfig> getSourceConfigs() throws DataVinesException;

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
}
