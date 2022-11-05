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

import io.datavines.common.config.*;
import io.datavines.common.config.enums.SourceType;
import io.datavines.common.entity.*;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.config.BaseDataQualityConfigurationBuilder;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.spi.PluginLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.datavines.engine.api.ConfigConstants.*;

public abstract class BaseJdbcConfigurationBuilder extends BaseDataQualityConfigurationBuilder {

    @Override
    protected EnvConfig getEnvConfig() {
        EnvConfig envConfig = new EnvConfig();
        envConfig.setEngine(jobExecutionInfo.getEngineType());
        return envConfig;
    }

    @Override
    protected List<SourceConfig> getSourceConfigs() throws DataVinesException {
        List<SourceConfig> sourceConfigs = new ArrayList<>();

        if (jobExecutionParameter.getConnectorParameter() != null) {
            ConnectorParameter connectorParameter = jobExecutionParameter.getConnectorParameter();
            SourceConfig sourceConfig = new SourceConfig();

            Map<String, Object> connectorParameterMap = new HashMap<>(connectorParameter.getParameters());
            connectorParameterMap.putAll(inputParameter);

            ConnectorFactory connectorFactory = PluginLoader
                    .getPluginLoader(ConnectorFactory.class)
                    .getNewPlugin(connectorParameter.getType());

            connectorParameterMap = connectorFactory.getConnectorParameterConverter().converter(connectorParameterMap);

            String outputTable = inputParameter.get(TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            connectorParameterMap.put(DRIVER, connectorFactory.getDialect().getDriver());
            inputParameter.put(REGEX_KEY, connectorFactory.getDialect().getRegexKey());
            inputParameter.put(NOT_REGEX_KEY, connectorFactory.getDialect().getNotRegexKey());
            inputParameter.put(SRC_CONNECTOR_TYPE, connectorParameter.getType());

            sourceConfig.setPlugin(connectorFactory.getCategory());
            sourceConfig.setConfig(connectorParameterMap);
            sourceConfig.setType(SourceType.NORMAL.getDescription());
            sourceConfigs.add(sourceConfig);
        }

        if (jobExecutionParameter.getConnectorParameter2() != null && jobExecutionParameter.getConnectorParameter2().getParameters() !=null) {
            ConnectorParameter connectorParameter2 = jobExecutionParameter.getConnectorParameter2();
            SourceConfig sourceConfig = new SourceConfig();

            Map<String, Object> connectorParameterMap = new HashMap<>(connectorParameter2.getParameters());
            connectorParameterMap.putAll(inputParameter);

            ConnectorFactory connectorFactory = PluginLoader
                    .getPluginLoader(ConnectorFactory.class)
                    .getNewPlugin(connectorParameter2.getType());

            connectorParameterMap = connectorFactory.getConnectorParameterConverter().converter(connectorParameterMap);

            String outputTable = inputParameter.get(TARGET_TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            connectorParameterMap.put(DRIVER, connectorFactory.getDialect().getDriver());

            sourceConfig.setPlugin(connectorFactory.getCategory());
            sourceConfig.setConfig(connectorParameterMap);
            sourceConfig.setType(SourceType.NORMAL.getDescription());
            sourceConfigs.add(sourceConfig);
        }

        String expectedType = jobExecutionInfo.getEngineType() + "_" + jobExecutionParameter.getExpectedType();
        if (StringUtils.isEmpty(expectedType)) {
            return sourceConfigs;
        }

        expectedValue = PluginLoader
                .getPluginLoader(ExpectedValue.class)
                .getNewPlugin(expectedType);

        if (expectedValue.isNeedDefaultDatasource()) {
            sourceConfigs.add(getValidateResultSourceConfig());
        }

        return sourceConfigs;
    }
}
