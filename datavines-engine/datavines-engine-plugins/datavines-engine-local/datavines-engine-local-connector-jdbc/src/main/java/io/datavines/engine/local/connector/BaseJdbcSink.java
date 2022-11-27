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
package io.datavines.engine.local.connector;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.common.config.enums.SinkType;
import io.datavines.common.enums.DataType;
import io.datavines.common.utils.StringUtils;
import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.connector.api.TypeConverter;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.local.api.LocalRuntimeEnvironment;
import io.datavines.engine.local.api.LocalSink;
import io.datavines.engine.local.api.entity.ConnectionItem;
import io.datavines.engine.local.api.entity.ResultList;
import io.datavines.engine.local.api.utils.FileUtils;
import io.datavines.engine.local.api.utils.LoggerFactory;
import io.datavines.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;


import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.datavines.engine.api.ConfigConstants.*;
import static io.datavines.engine.api.EngineConstants.PLUGIN_TYPE;

@Slf4j
public abstract class BaseJdbcSink implements LocalSink {

    protected Config config = new Config();

    @Override
    public void setConfig(Config config) {
        if(config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {
        List<String> requiredOptions = Arrays.asList("url", "user", "password");

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x->{
            if(!config.has(x)){
                nonExistsOptions.add(x);
            }
        });

        if (!nonExistsOptions.isEmpty()) {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new CheckResult(true, "");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment env) {

    }

    @Override
    public void output(List<ResultList> resultList, LocalRuntimeEnvironment env) {

        if(env.getMetadataConnection() == null) {
            env.setMetadataConnection(getConnectionItem());
        }

        Map<String,String> inputParameter = new HashMap<>();
        setExceptedValue(config, resultList, inputParameter);
        try {
            switch (SinkType.of(config.getString(PLUGIN_TYPE))){
                case ERROR_DATA:
                    sinkErrorData();
                    break;
                case ACTUAL_VALUE:
                    if(!checkTableExist(env, "dv_actual_values")) {
                        createTable(env, getActualValueTableSql());
                    }
                case VALIDATE_RESULT:
                    if(!checkTableExist(env, "dv_job_execution_result")) {
                        createTable(env, getExecutionResultTableSql());
                    }

                    String sql = config.getString(SQL);
                    sql = PlaceholderUtils.replacePlaceholders(sql, inputParameter,true);
                    log.info("execute " + config.getString(PLUGIN_TYPE) + " output sql : {}", sql);
                    executeInsert(sql, env);
                    break;
                default:
                    break;
            }
        } catch (SQLException e){
            log.error("sink error : {}", e.getMessage());
        }
    }

    private void executeInsert(String sql, LocalRuntimeEnvironment env) throws SQLException {
        Statement statement =  env.getMetadataConnection().getConnection().createStatement();
        statement.execute(sql);
        statement.close();
    }

    private boolean checkTableExist(LocalRuntimeEnvironment env, String tableName) throws SQLException{
        //定义一个变量标示
        boolean flag = false ;
        //一个查询该表所有的语句。
        String sql = "SELECT COUNT(*) FROM "+ tableName ;
        Statement statement = null;
        try{
            statement =  env.getMetadataConnection().getConnection().createStatement();
            statement.executeQuery(sql);
            flag =  true;
        } catch(Exception e) {
            log.warn("table {} is not exist", tableName);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return flag;
    }

    private void createTable(LocalRuntimeEnvironment env, String createTableSql) throws SQLException{
        Statement statement =  env.getMetadataConnection().getConnection().createStatement();
        statement.execute(createTableSql);
        statement.close();
    }

    protected abstract String getExecutionResultTableSql();

    protected abstract String getActualValueTableSql();

    private ConnectionItem getConnectionItem() {
        return new ConnectionItem(config);
    }

    protected abstract String buildCreateTableSql(String tableName, String header);

    private String buildInsertSql(String tableName, String header) {
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO ").append(tableName).append(" (");
        String[] headerList = header.split(S001);
        List<String> columnList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();
        for (String column: headerList) {
            String[] columnSplit = column.split(DOUBLE_AT);
            columnList.add(columnSplit[0]);
            placeholderList.add("?");
        }
        insertSql.append(String.join(",", columnList));
        insertSql.append(" )");
        insertSql.append(" values(");
        insertSql.append(String.join(",", placeholderList));
        insertSql.append(" )");
        return insertSql.toString();
    }

    private void sinkErrorData() throws SQLException {
        String tableName = config.getString(ERROR_DATA_FILE_NAME);
        String filePath = config.getString(ERROR_DATA_DIR) + "/" + tableName + ".csv";
        log.info("log file path : {}", filePath);
        File file = new File(filePath);
        if (file.exists()) {
            List<String> headerList = FileUtils.readPartFileContent(filePath,0,1);
            if (CollectionUtils.isNotEmpty(headerList)) {
                String header = headerList.get(0);
                String[] headerTypeList = header.split(S001);
                Map<Integer,String> typeMap = new HashMap<>();
                for (int i=0; i<headerTypeList.length; i++) {
                    String[] columnSplit = headerTypeList[i].split(DOUBLE_AT);
                    typeMap.put(i,columnSplit[1]);
                }
                String createTableSql = buildCreateTableSql(tableName, header);
                Connection connection = getConnectionItem().getConnection();
                connection.createStatement().execute(createTableSql);
                PreparedStatement statement = connection.prepareStatement(buildInsertSql(tableName, header));
                int skipLine = 1;
                int limit = 1000;
                List<String> rowList = null;
                while(CollectionUtils.isNotEmpty(rowList = FileUtils.readPartFileContent(filePath,skipLine,limit))) {
                    for (String row: rowList) {
                        String[] rowDataList = row.split(S001);
                        for (int i=0; i<rowDataList.length; i++) {
                            String rowContent = "null".equalsIgnoreCase(rowDataList[i]) ? null : rowDataList[i];
                            try{
                                switch (DataType.valueOf(typeMap.get(i))) {
                                    case NULL_TYPE:
                                        statement.setNull(i+1, 0);
                                        break;
                                    case BOOLEAN_TYPE:
                                        statement.setBoolean(i+1, Boolean.parseBoolean(rowContent));
                                        break;
                                    case BYTE_TYPE:
                                        if (StringUtils.isNotEmpty(rowContent)) {
                                            statement.setByte(i+1, Byte.parseByte(rowContent));
                                        } else {
                                            statement.setByte(i+1,Byte.parseByte(""));
                                        }
                                        break;
                                    case SHORT_TYPE:
                                        if (StringUtils.isNotEmpty(rowContent)) {
                                            statement.setShort(i+1, Short.parseShort(rowContent));
                                        } else {
                                            statement.setShort(i+1, Short.parseShort("0"));
                                        }
                                        break;
                                    case INT_TYPE :
                                        if (StringUtils.isNotEmpty(rowContent)) {
                                            statement.setInt(i+1, Integer.parseInt(rowContent));
                                        } else {
                                            statement.setInt(i+1, 0);
                                        }
                                        break;
                                    case LONG_TYPE:
                                        if (StringUtils.isNotEmpty(rowContent)) {
                                            statement.setLong(i+1, Long.parseLong(rowContent));
                                        } else {
                                            statement.setLong(i+1, 0);
                                        }
                                        break;
                                    case FLOAT_TYPE:
                                        if (StringUtils.isNotEmpty(rowContent)) {
                                            statement.setLong(i+1, Long.parseLong(rowContent));
                                        } else {
                                            statement.setLong(i+1, 0);
                                        }
                                        break;
                                    case DOUBLE_TYPE:
                                        if (StringUtils.isNotEmpty(rowContent)) {
                                            statement.setDouble(i+1, Double.parseDouble(rowContent));
                                        } else {
                                            statement.setDouble(i+1, 0);
                                        }
                                        break;
                                    case TIME_TYPE:
                                    case DATE_TYPE:
                                    case TIMESTAMP_TYPE:
                                        if (StringUtils.isNotEmpty(rowContent)) {
                                            statement.setString(i+1,rowContent);
                                        } else {
                                            statement.setString(i+1,null);
                                        }
                                        break;
                                    case STRING_TYPE :
                                        statement.setString(i+1, rowContent);
                                        break;
                                    case BYTES_TYPE:
                                        statement.setBytes(i+1, String.valueOf(rowContent).getBytes());
                                        break;
                                    case BIG_DECIMAL_TYPE:
                                        if (StringUtils.isNotEmpty(rowContent)) {
                                            statement.setBigDecimal(i+1, new BigDecimal(rowContent));
                                        } else {
                                            statement.setBigDecimal(i+1, null);
                                        }
                                        break;
                                    case OBJECT:
                                        break;
                                    default:
                                        break;
                                }
                            } catch (SQLException exception) {
                                log.error("transform data type error", exception);
                            }
                        }

                        try {
                            statement.addBatch();
                        } catch (SQLException e) {
                            log.error("insert data error", e);
                        }
                    }

                    skipLine += limit;
                }

                statement.executeBatch();
                statement.close();
                connection.close();
            }

            file.delete();
        }
    }
}
