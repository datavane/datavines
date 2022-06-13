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
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.entity.job.DataQualityJobParameter;
import io.datavines.common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataQualityTaskParameterBuilder implements ParameterBuilder {

    @Override
    public List<String> buildTaskParameter(String jobParameter, ConnectionInfo srcConnectionInfo, ConnectionInfo targetConnectionInfo) {
        List<DataQualityJobParameter> jobParameters = JSONUtils.toList(jobParameter, DataQualityJobParameter.class);

        if (CollectionUtils.isNotEmpty(jobParameters)) {
            List<String> taskParameters = new ArrayList<>();
            jobParameters.forEach(jobParam -> {
                TaskParameter taskParameter = new TaskParameter();
                taskParameter.setMetricType(jobParam.getMetricType());
                Map<String,Object> metricParameters = jobParam.getMetricParameter();
                String database = (String)metricParameters.get("database");
                metricParameters.remove("database");
                taskParameter.setMetricParameter(metricParameters);
                taskParameter.setExpectedType(jobParam.getExpectedType());
                taskParameter.setExpectedParameter(jobParam.getExpectedParameter());
                taskParameter.setResultFormula(jobParam.getResultFormula());
                taskParameter.setOperator(jobParam.getOperator());
                taskParameter.setThreshold(jobParam.getThreshold());

                ConnectorParameter srcConnectorParameter = new ConnectorParameter();
                srcConnectorParameter.setType(srcConnectionInfo.getType());
                Map<String,Object> srcConnectorParameterMap = srcConnectionInfo.configMap();
                srcConnectorParameterMap.put("database", database);
                srcConnectorParameter.setParameters(srcConnectorParameterMap);
                taskParameter.setSrcConnectorParameter(srcConnectorParameter);

                String taskParameterStr = JSONUtils.toJsonString(taskParameter);
                taskParameters.add(taskParameterStr);
            });

            return taskParameters;
        }

        return null;
    }
}
