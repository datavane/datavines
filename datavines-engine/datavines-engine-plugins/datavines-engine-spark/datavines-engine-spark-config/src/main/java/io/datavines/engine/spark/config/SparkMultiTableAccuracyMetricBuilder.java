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
import static io.datavines.engine.api.ConfigConstants.EXPECTED_VALUE;

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
        SinkConfig errorDataSinkConfig = getErrorSinkConfig();
        if (errorDataSinkConfig != null) {
            sinkConfigs.add(errorDataSinkConfig);
        }

        configuration.setSinkParameters(sinkConfigs);
    }

    private String getOnClause(List<MappingColumn> mappingColumnList, Map<String,String> inputParameterValueResult) {
        //get on clause
        String[] columnList = new String[mappingColumnList.size()];
        for (int i = 0; i < mappingColumnList.size(); i++) {
            MappingColumn column = mappingColumnList.get(i);
            columnList[i] = getCoalesceString(inputParameterValueResult.get(TABLE),column.getColumn())
                    + column.getOperator()
                    + getCoalesceString(inputParameterValueResult.get(TABLE2),column.getColumn2());
        }

        return String.join(AND,columnList);
    }

    private String getWhereClause(List<MappingColumn> mappingColumnList,Map<String,String> inputParameterValueResult) {
        String columnNotNull = "( NOT (" + getColumnIsNullStr(inputParameterValueResult.get(TABLE),getColumnListInTable(mappingColumnList)) + " ))";
        String columnIsNull2 = "( " + getColumnIsNullStr(inputParameterValueResult.get(TABLE2),getColumnListInTable2(mappingColumnList)) + " )";

        return columnNotNull + AND + columnIsNull2;
    }

    private String getCoalesceString(String table, String column) {
        return "coalesce(" + table + "." + column + ", '')";
    }

    private String getColumnIsNullStr(String table, List<String> columns) {
        String[] columnList = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            columnList[i] = table + "." + column + " IS NULL";
        }
        return  String.join(AND, columnList);
    }

    private List<String> getColumnListInTable(List<MappingColumn> mappingColumns) {
        List<String> list = new ArrayList<>();
        mappingColumns.forEach(item ->
                list.add(item.getColumn())
        );

        return list;
    }

    private List<String> getColumnListInTable2(List<MappingColumn> mappingColumns) {
        List<String> list = new ArrayList<>();
        mappingColumns.forEach(item ->
                list.add(item.getColumn2())
        );

        return list;
    }
}
