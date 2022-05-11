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
package io.datavines.engine.config;

import io.datavines.common.config.DataVinesQualityConfig;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.entity.TaskInfo;
import io.datavines.common.entity.TaskParameter;

import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;

import java.util.Map;

public class DataVinesConfigurationManager {

    private static DataVinesQualityConfig buildDataQualityConfiguration(DataQualityConfigurationBuilder builder) throws DataVinesException {
        builder.buildName();
        builder.buildEnvConfig();
        builder.buildSourceConfigs();
        builder.buildTransformConfigs();
        builder.buildSinkConfigs();
        return builder.build();
    }

    public static DataVinesQualityConfig generateConfiguration(
            Map<String, String> inputParameter,
            TaskInfo taskInfo,
            ConnectionInfo connectionInfo) throws DataVinesException {

        if(taskInfo == null){
            throw new DataVinesException("taskInfo can not be null");
        }

        if(taskInfo.getTaskParameter() == null){
            throw new DataVinesException("task parameter can not be null");
        }

        TaskParameter taskParameter = taskInfo.getTaskParameter();

        String metricType = taskParameter.getMetricType();
        if (StringUtils.isEmpty(metricType)) {
            throw new DataVinesException("metric type can not be null");
        }

        SqlMetric sqlMetric = PluginLoader
                .getPluginLoader(SqlMetric.class)
                .getNewPlugin(metricType);

        if (sqlMetric == null) {
            throw new DataVinesException("can not find the metric: " + metricType);
        }

        DataQualityConfigurationBuilder builder = PluginLoader
                .getPluginLoader(DataQualityConfigurationBuilder.class)
                .getOrCreatePlugin(taskInfo.getEngineType() + "_" + sqlMetric.getType().getDescription());
        builder.init(inputParameter, taskInfo, connectionInfo);

        return buildDataQualityConfiguration(builder);
    }

}
