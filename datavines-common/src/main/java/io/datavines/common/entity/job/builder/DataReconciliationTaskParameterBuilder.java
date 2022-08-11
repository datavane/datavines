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
import io.datavines.common.entity.MappingColumn;
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.entity.job.DataReconciliationJobParameter;
import io.datavines.common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.datavines.common.CommonConstants.*;

public class DataReconciliationTaskParameterBuilder implements ParameterBuilder {

    @Override
    public List<String> buildTaskParameter(String jobParameter, ConnectionInfo srcConnectionInfo, ConnectionInfo targetConnectionInfo) {
        List<DataReconciliationJobParameter> jobParameters = JSONUtils.toList(jobParameter, DataReconciliationJobParameter.class);

        if (CollectionUtils.isNotEmpty(jobParameters)) {
            List<String> taskParameters = new ArrayList<>();
            jobParameters.forEach(jobParam -> {
                TaskParameter taskParameter = new TaskParameter();
                taskParameter.setMetricType(jobParam.getMetricType());
                Map<String,Object> metricParameters = jobParam.getMetricParameter();
                String database = (String)metricParameters.get("database");
                metricParameters.remove("database");
                metricParameters.put("metric_database", database);
                metricParameters.putAll(jobParam.getMetricParameter2());
                metricParameters.put("on_clause", getOnClause(jobParam.getMappingColumns(),metricParameters));
                metricParameters.put("where_clause", getWhereClause(jobParam.getMappingColumns(),metricParameters));
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

                ConnectorParameter targetConnectorParameter = new ConnectorParameter();
                targetConnectorParameter.setType(targetConnectionInfo.getType());
                Map<String,Object> targetConnectorParameterMap = targetConnectionInfo.configMap();
                targetConnectorParameterMap.put("database", jobParam.getMetricParameter2().get("database2"));
                targetConnectorParameter.setParameters(targetConnectorParameterMap);
                taskParameter.setTargetConnectorParameter(targetConnectorParameter);

                String taskParameterStr = JSONUtils.toJsonString(taskParameter);
                taskParameters.add(taskParameterStr);
            });

            return taskParameters;
        }

        return null;
    }

    public static String getOnClause(List<MappingColumn> mappingColumnList, Map<String,Object> inputParameterValueResult) {
        //get on clause
        String[] columnList = new String[mappingColumnList.size()];
        for (int i = 0; i < mappingColumnList.size(); i++) {
            MappingColumn column = mappingColumnList.get(i);
            columnList[i] = getCoalesceString((String) inputParameterValueResult.get(TABLE),column.getSrcColumn())
                    + column.getOperator()
                    + getCoalesceString((String) inputParameterValueResult.get(TABLE2),column.getTargetColumn());
        }

        return String.join(AND,columnList);
    }

    public static String getWhereClause(List<MappingColumn> mappingColumnList,Map<String,Object> inputParameterValueResult) {
        String srcColumnNotNull = "( NOT (" + getSrcColumnIsNullStr((String) inputParameterValueResult.get(TABLE),getSrcColumnList(mappingColumnList)) + " ))";
        String targetColumnIsNull = "( " + getSrcColumnIsNullStr((String) inputParameterValueResult.get(TABLE2),getTargetColumnList(mappingColumnList)) + " )";

        return srcColumnNotNull + AND + targetColumnIsNull;
    }

    private static String getCoalesceString(String table, String column) {
        return "coalesce(" + table + "." + column + ", '')";
    }

    private static String getSrcColumnIsNullStr(String table,List<String> columns) {
        String[] columnList = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            columnList[i] = table + "." + column + " IS NULL";
        }
        return  String.join(AND, columnList);
    }

    public static List<String> getSrcColumnList(List<MappingColumn> mappingColumns) {
        List<String> list = new ArrayList<>();
        mappingColumns.forEach(item ->
                list.add(item.getSrcColumn())
        );

        return list;
    }

    public static List<String> getTargetColumnList(List<MappingColumn> mappingColumns) {
        List<String> list = new ArrayList<>();
        mappingColumns.forEach(item ->
                list.add(item.getTargetColumn())
        );

        return list;
    }
}
