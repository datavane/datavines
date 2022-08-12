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
import io.datavines.common.entity.MappingColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.datavines.common.CommonConstants.AND;
import static io.datavines.common.CommonConstants.TABLE;
import static io.datavines.common.CommonConstants.TABLE2;

public class SparkMultiTableAccuracyMetricBuilder extends BaseSparkConfigurationBuilder {

    @Override
    public void buildTransformConfigs() {
        super.buildTransformConfigs();
        List<MappingColumn> mappingColumns = JSONUtils.toList(inputParameter.get("mappingColumns"),MappingColumn.class);
        inputParameter.put("on_clause", getOnClause(mappingColumns,inputParameter));
        inputParameter.put("where_clause", getWhereClause(mappingColumns, inputParameter));
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

    public static String getOnClause(List<MappingColumn> mappingColumnList, Map<String,String> inputParameterValueResult) {
        //get on clause
        String[] columnList = new String[mappingColumnList.size()];
        for (int i = 0; i < mappingColumnList.size(); i++) {
            MappingColumn column = mappingColumnList.get(i);
            columnList[i] = getCoalesceString(inputParameterValueResult.get(TABLE),column.getSrcColumn())
                    + column.getOperator()
                    + getCoalesceString(inputParameterValueResult.get(TABLE2),column.getTargetColumn());
        }

        return String.join(AND,columnList);
    }

    public static String getWhereClause(List<MappingColumn> mappingColumnList,Map<String,String> inputParameterValueResult) {
        String srcColumnNotNull = "( NOT (" + getSrcColumnIsNullStr(inputParameterValueResult.get(TABLE),getSrcColumnList(mappingColumnList)) + " ))";
        String targetColumnIsNull = "( " + getSrcColumnIsNullStr(inputParameterValueResult.get(TABLE2),getTargetColumnList(mappingColumnList)) + " )";

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
