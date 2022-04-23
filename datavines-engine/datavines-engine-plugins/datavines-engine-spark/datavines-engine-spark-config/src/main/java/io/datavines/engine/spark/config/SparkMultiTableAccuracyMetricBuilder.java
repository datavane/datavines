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

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.datavines.common.config.SinkConfig;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.StringUtils;
import io.datavines.engine.config.entity.MappingColumn;
import io.datavines.metric.api.MetricConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.datavines.engine.config.ConfigConstants.*;

public class SparkMultiTableAccuracyMetricBuilder extends BaseSparkConfigurationBuilder {

    @Override
    public void buildTransformConfigs() {
        List<MappingColumn> mappingColumnList = getMappingColumnList(inputParameter.get(MAPPING_COLUMNS));
        //get on clause
        inputParameter.put(ON_CLAUSE, getOnClause(mappingColumnList,inputParameter));
        //get where clause
        inputParameter.put(WHERE_CLAUSE, getWhereClause(mappingColumnList,inputParameter));

        super.buildTransformConfigs();
    }

    @Override
    public void buildSinkConfigs() throws DataVinesException {
        List<SinkConfig> sinkConfigs = new ArrayList<>();
        //get the actual value storage parameter
        SinkConfig actualValueSinkConfig = getDefaultSinkConfig(SparkSinkSqlBuilder.getActualValueSql(),  "dv_actual_values");
        sinkConfigs.add(actualValueSinkConfig);

        String taskSinkSql = SparkSinkSqlBuilder.getDefaultSinkSql();
        if (StringUtils.isEmpty(expectedValue.getOutputTable())) {
            taskSinkSql = taskSinkSql.replaceAll("full join \\$\\{expected_table}","");
        }

        //get the task data storage parameter
        SinkConfig taskResultSinkConfig = getDefaultSinkConfig(taskSinkSql, "dv_task_result");
        sinkConfigs.add(taskResultSinkConfig);

        //todo
        //get the error data storage parameter
        //support file(hdfs/minio/s3)/es

        configuration.setSinkParameters(sinkConfigs);
    }

    private List<MappingColumn> getMappingColumnList(String mappingColumns) {
        ArrayNode mappingColumnList = JSONUtils.parseArray(mappingColumns);
        List<MappingColumn> list = new ArrayList<>();
        mappingColumnList.forEach(item -> {
            MappingColumn column = new MappingColumn(
                    String.valueOf(item.get(COLUMN)).replace("\"",""),
                    String.valueOf(item.get(OPERATOR)).replace("\""," "),
                    String.valueOf(item.get(TARGET_COLUMN)).replace("\"",""));
            list.add(column);
        });

        return list;
    }

    private List<String> getSrcColumnList(List<MappingColumn> mappingColumns) {
        List<String> list = new ArrayList<>();
        mappingColumns.forEach(item ->
                list.add(item.getSrcField())
        );

        return list;
    }

    private List<String> getTargetColumnList(List<MappingColumn> mappingColumns) {
        List<String> list = new ArrayList<>();
        mappingColumns.forEach(item ->
                list.add(item.getTargetField())
        );

        return list;
    }

    private String getOnClause(List<MappingColumn> mappingColumnList,Map<String,String> inputParameterValueResult) {
        //get on clause
        String[] columnList = new String[mappingColumnList.size()];
        for (int i = 0; i < mappingColumnList.size(); i++) {
            MappingColumn column = mappingColumnList.get(i);
            columnList[i] = getCoalesceString(inputParameterValueResult.get(TABLE),column.getSrcField())
                    + column.getOperator()
                    + getCoalesceString(inputParameterValueResult.get(TARGET_TABLE),column.getTargetField());
        }

        return String.join(AND,columnList);
    }

    private String getWhereClause(List<MappingColumn> mappingColumnList,Map<String,String> inputParameterValueResult) {
        String srcColumnNotNull = "( NOT (" + getSrcColumnIsNullStr(inputParameterValueResult.get(TABLE),getSrcColumnList(mappingColumnList)) + " ))";
        String targetColumnIsNull = "( " + getSrcColumnIsNullStr(inputParameterValueResult.get(TARGET_TABLE),getTargetColumnList(mappingColumnList)) + " )";

        return srcColumnNotNull + AND + targetColumnIsNull;
    }

    private String getCoalesceString(String table, String column) {
        return "coalesce(" + table + "." + column + ", '')";
    }

    private String getSrcColumnIsNullStr(String table,List<String> columns) {
        String[] columnList = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            columnList[i] = table + "." + column + " IS NULL";
        }
        return  String.join(AND, columnList);
    }
}
