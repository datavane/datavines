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
import io.datavines.common.entity.job.BaseJobParameter;
import io.datavines.common.entity.job.DataQualityJobParameter;
import io.datavines.common.exception.DataVinesException;
import io.datavines.engine.config.MetricParserUtils;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SparkMultiTableValueComparisonMetricBuilder extends BaseSparkConfigurationBuilder {

    @Override
    public void buildTransformConfigs() {
        List<BaseJobParameter> metricJobParameterList = jobExecutionParameter.getMetricJobParameterList();
        if (CollectionUtils.isNotEmpty(metricJobParameterList)) {
            for (BaseJobParameter parameter : metricJobParameterList) {
                String metricUniqueKey = getMetricUniqueKey(parameter);
                Map<String, String> metricInputParameter = metric2InputParameter.get(metricUniqueKey);

                String metricType = parameter.getMetricType();
                SqlMetric sqlMetric = PluginLoader
                        .getPluginLoader(SqlMetric.class)
                        .getNewPlugin(metricType);

                MetricParserUtils.operateInputParameter(metricInputParameter, sqlMetric, jobExecutionInfo);
            }
        }
    }

    @Override
    public void buildSinkConfigs() throws DataVinesException {

        List<SinkConfig> sinkConfigs = new ArrayList<>();

        List<BaseJobParameter> metricJobParameterList = jobExecutionParameter.getMetricJobParameterList();
        if (CollectionUtils.isNotEmpty(metricJobParameterList)) {
            for (BaseJobParameter parameter : metricJobParameterList) {
                String metricUniqueKey = getMetricUniqueKey(parameter);
                Map<String, String> metricInputParameter = metric2InputParameter.get(metricUniqueKey);
                metricInputParameter.put("expected_value", "expected_value");

                //get the task data storage parameter
                SinkConfig taskResultSinkConfig = getValidateResultDataSinkConfig(
                        null, SparkSinkSqlBuilder.getMultiTableComparisonSinkSql(), "dv_job_execution_result", metricInputParameter);
                sinkConfigs.add(taskResultSinkConfig);
            }
        }

        configuration.setSinkParameters(sinkConfigs);
    }
}
