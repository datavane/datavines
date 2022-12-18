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
import io.datavines.common.config.enums.SinkType;
import io.datavines.common.entity.*;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.config.BaseDataQualityConfigurationBuilder;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.spi.PluginLoader;
import io.datavines.storage.api.StorageFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.datavines.common.CommonConstants.TABLE2;
import static io.datavines.engine.api.ConfigConstants.*;

public abstract class BaseSparkConfigurationBuilder extends BaseDataQualityConfigurationBuilder {

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

            String outputTable = connectorParameter.getParameters().get(DATABASE) + "_" + inputParameter.get(TABLE);
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            connectorParameterMap.put(DRIVER, connectorFactory.getDialect().getDriver());
            inputParameter.put(TABLE, outputTable);

            sourceConfig.setPlugin(connectorFactory.getCategory());
            sourceConfig.setConfig(connectorParameterMap);
            sourceConfigs.add(sourceConfig);
        }

        if (jobExecutionParameter.getConnectorParameter2() != null && jobExecutionParameter.getConnectorParameter2().getParameters() !=null) {
            ConnectorParameter connectorParameter2 = jobExecutionParameter.getConnectorParameter2();
            SourceConfig sourceConfig = new SourceConfig();

            Map<String, Object> connectorParameterMap = new HashMap<>(connectorParameter2.getParameters());
            connectorParameterMap.put(TABLE, inputParameter.get(TABLE2));

            ConnectorFactory connectorFactory = PluginLoader
                    .getPluginLoader(ConnectorFactory.class)
                    .getNewPlugin(connectorParameter2.getType());

            connectorParameterMap = connectorFactory.getConnectorParameterConverter().converter(connectorParameterMap);

            String outputTable = connectorParameter2.getParameters().get(DATABASE) + "_" + inputParameter.get(TABLE2) + "2";
            connectorParameterMap.put(OUTPUT_TABLE, outputTable);
            connectorParameterMap.put(DRIVER, connectorFactory.getDialect().getDriver());
            inputParameter.put(TABLE2, outputTable);

            sourceConfig.setPlugin(connectorFactory.getCategory());
            sourceConfig.setConfig(connectorParameterMap);
            sourceConfigs.add(sourceConfig);
        }

        inputParameter.put("actual_value", "actual_value");

        String expectedType = jobExecutionInfo.getEngineType() + "_" + jobExecutionParameter.getExpectedType();
        if (StringUtils.isEmpty(jobExecutionParameter.getExpectedType())) {
            return sourceConfigs;
        }

        expectedValue = PluginLoader
                .getPluginLoader(ExpectedValue.class)
                .getNewPlugin(expectedType);

        if (expectedValue.isNeedDefaultDatasource()) {
            sourceConfigs.add(getValidateResultDataSourceConfig());
        }

        return sourceConfigs;
    }

    protected SinkConfig getErrorSinkConfig() {
        SinkConfig errorDataSinkConfig = null;
        if (StringUtils.isNotEmpty(jobExecutionInfo.getErrorDataStorageType())
                && StringUtils.isNotEmpty(jobExecutionInfo.getErrorDataStorageParameter())) {
            errorDataSinkConfig = new SinkConfig();
            errorDataSinkConfig.setType(SinkType.ERROR_DATA.getDescription());

            Map<String, Object> connectorParameterMap = new HashMap<>(JSONUtils.toMap(jobExecutionInfo.getErrorDataStorageParameter(),String.class, Object.class));
            connectorParameterMap.putAll(inputParameter);
            StorageFactory storageFactory = PluginLoader
                    .getPluginLoader(StorageFactory.class)
                    .getNewPlugin(jobExecutionInfo.getErrorDataStorageType());

            if (storageFactory != null) {
                connectorParameterMap = storageFactory.getStorageConnector().getParamMap(connectorParameterMap);
                errorDataSinkConfig.setPlugin(storageFactory.getCategory());
                connectorParameterMap.put(ERROR_DATA_FILE_NAME, jobExecutionInfo.getErrorDataFileName());
                connectorParameterMap.put(TABLE, jobExecutionInfo.getErrorDataFileName());
                connectorParameterMap.put(SQL, "SELECT * FROM "+ inputParameter.get(INVALIDATE_ITEMS_TABLE));
                errorDataSinkConfig.setConfig(connectorParameterMap);
            }
        }

        return errorDataSinkConfig;
    }
}
