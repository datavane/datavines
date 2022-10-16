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
package io.datavines.common.entity.job.builder;

import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.entity.ConnectorParameter;
import io.datavines.common.entity.JobExecutionParameter;
import io.datavines.common.entity.job.DataReconciliationJobParameter;
import io.datavines.common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataReconciliationTaskParameterBuilder implements ParameterBuilder {

    @Override
    public List<String> buildTaskParameter(String jobParameter, ConnectionInfo connectionInfo, ConnectionInfo connectionInfo2) {
        List<DataReconciliationJobParameter> jobParameters = JSONUtils.toList(jobParameter, DataReconciliationJobParameter.class);

        if (CollectionUtils.isNotEmpty(jobParameters)) {
            List<String> taskParameters = new ArrayList<>();
            jobParameters.forEach(jobParam -> {
                JobExecutionParameter jobExecutionParameter = new JobExecutionParameter();
                jobExecutionParameter.setMetricType(jobParam.getMetricType());
                Map<String,Object> metricParameters = jobParam.getMetricParameter();
                String database = (String)metricParameters.get("database");
                metricParameters.remove("database");
                metricParameters.put("metric_database", database);
                metricParameters.putAll(jobParam.getMetricParameter2());
                metricParameters.put("mappingColumns", JSONUtils.toJsonString(jobParam.getMappingColumns()));
                jobExecutionParameter.setMetricParameter(metricParameters);
                jobExecutionParameter.setExpectedType(jobParam.getExpectedType());
                jobExecutionParameter.setExpectedParameter(jobParam.getExpectedParameter());
                jobExecutionParameter.setResultFormula(jobParam.getResultFormula());
                jobExecutionParameter.setOperator(jobParam.getOperator());
                jobExecutionParameter.setThreshold(jobParam.getThreshold());

                ConnectorParameter connectorParameter = new ConnectorParameter();
                connectorParameter.setType(connectionInfo.getType());
                Map<String,Object> connectorParameterMap = connectionInfo.configMap();
                connectorParameterMap.put("database", database);
                connectorParameter.setParameters(connectorParameterMap);
                jobExecutionParameter.setConnectorParameter(connectorParameter);

                ConnectorParameter connectorParameter2 = new ConnectorParameter();
                connectorParameter2.setType(connectionInfo2.getType());
                Map<String,Object> connectorParameter2Map = connectionInfo2.configMap();
                connectorParameter2Map.put("database", jobParam.getMetricParameter2().get("database2"));
                connectorParameter2.setParameters(connectorParameter2Map);
                jobExecutionParameter.setConnectorParameter2(connectorParameter2);

                String taskParameterStr = JSONUtils.toJsonString(jobExecutionParameter);
                taskParameters.add(taskParameterStr);
            });

            return taskParameters;
        }

        return null;
    }

}
