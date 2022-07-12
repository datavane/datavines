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
package io.datavines.engine.jdbc.connector;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.common.config.enums.SinkType;
import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.jdbc.api.JdbcRuntimeEnvironment;
import io.datavines.engine.jdbc.api.JdbcSink;
import io.datavines.engine.jdbc.api.entity.ResultList;
import io.datavines.engine.jdbc.api.utils.FileUtils;
import io.datavines.engine.jdbc.api.utils.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;


import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.datavines.engine.api.EngineConstants.PLUGIN_TYPE;

public class BaseJdbcSink implements JdbcSink {

    private Logger logger = LoggerFactory.getLogger(BaseJdbcSink.class);

    private Config config = new Config();

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
    public void output(List<ResultList> resultList, JdbcRuntimeEnvironment env) {

        if(env.getMetadataConnection() == null) {
            env.setMetadataConnection(getConnection());
        }

        Map<String,String> inputParameter = new HashMap<>();
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultList.forEach(item -> {
                if(item != null) {
                    item.getResultList().forEach(x -> {
                        x.forEach((k,v) -> {
                            inputParameter.put(k, String.valueOf(v));
                        });
                    });
                }
            });
        }

        try {
            switch (SinkType.of(config.getString(PLUGIN_TYPE))){
                case ERROR_DATA:
                    sinkErrorData();
                    break;
                case ACTUAL_VALUE:
                case TASK_RESULT:
                    String sql = config.getString("sql");
                    sql = PlaceholderUtils.replacePlaceholders(sql, inputParameter,true);
                    logger.info("execute output sql : {}", sql);
                    executeInsert(sql, env);
                    break;
                default:
                    break;
            }
        } catch (SQLException e){
            logger.error("sink error : {}", e.getMessage());
        }
    }

    private void executeInsert(String sql, JdbcRuntimeEnvironment env) throws SQLException {
        Statement statement =  env.getMetadataConnection().createStatement();
        statement.execute(sql);
        statement.close();
    }

    private Connection getConnection() {
        return ConnectionUtils.getConnection(config);
    }

    private String buildCreateTableSql(String tableName, String header) {
        StringBuilder createTableSql = new StringBuilder();
        createTableSql.append("CREATE TABLE ").append(tableName).append(" (");
        String[] headerList = header.split("\001");
        List<String> columnList = new ArrayList<>();
        for (String column: headerList) {
            String[] columnSplit = column.split("@@");
            if ("VARCHAR".equalsIgnoreCase(columnSplit[1])) {
                columnList.add(columnSplit[0]+" TEXT NULL");
            } else {
                columnList.add(columnSplit[0]+" "+columnSplit[1]+" NULL");
            }

        }
        createTableSql.append(String.join(",", columnList));
        createTableSql.append(" )");
        return createTableSql.toString();
    }

    private String buildInsertSql(String tableName, String header) {
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO ").append(tableName).append(" (");
        String[] headerList = header.split("\001");
        List<String> columnList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();
        for (String column: headerList) {
            String[] columnSplit = column.split("@@");
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
        //判断错误数据文件是否存在，如果存在则读取第一行数据
        //获取header生成建表语句
        //执行drop if exist
        //执行建表语句
        //执行批量插入语句
        String tableName = config.getString("metric_name").replace("'","")
                            + "_" + config.getString("task_id");
        String filePath = config.getString("error_data_path") + "/" + tableName + ".csv";
        logger.info("log file path : {}", filePath);
        File file = new File(filePath);
        if (file.exists()) {
            List<String> headerList = FileUtils.readPartFileContent(filePath,0,1);
            if (CollectionUtils.isNotEmpty(headerList)) {
                String header = headerList.get(0);
                String[] headerTypeList = header.split("\001");
                Map<Integer,String> typeMap = new HashMap<>();
                for (int i=0; i<headerTypeList.length; i++) {
                    String[] columnSplit = headerTypeList[i].split("@@");
                    typeMap.put(i,columnSplit[1]);
                }
                String createTableSql = buildCreateTableSql(tableName, header);
                Connection connection = getConnection();
                connection.createStatement().execute(createTableSql);
                PreparedStatement statement = connection.prepareStatement(buildInsertSql(tableName, header));
                int skipLine = 1;
                int limit = 1000;
                List<String> rowList = null;
                while(CollectionUtils.isNotEmpty(rowList = FileUtils.readPartFileContent(filePath,skipLine,limit))) {
                    for (String row: rowList) {
                        String[] rowDataList = row.split("\001");
                        for (int i=0; i<rowDataList.length; i++) {
                            String rowContent = "null".equalsIgnoreCase(rowDataList[i]) ? null:rowDataList[i];
                            try{
                                switch (typeMap.get(i).toUpperCase()) {
                                    case "VARCHAR":
                                    case "TEXT":
                                        statement.setString(i+1,rowContent);
                                        break;
                                    case "BIGINT":
                                    case "INT":
                                        if (rowContent != null) {
                                            statement.setInt(i+1, Integer.valueOf(rowContent));
                                        } else {
                                            statement.setInt(i+1, 0);
                                        }

                                        break;
                                    case "DATETIME":
                                        if (!"".equalsIgnoreCase(rowContent)) {
                                            statement.setString(i+1,rowContent);
                                        } else {
                                            statement.setString(i+1,null);
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            } catch (SQLException exception) {
                                logger.error("insert data error", exception);
                            }
                        }

                        try {
                            statement.addBatch();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    skipLine += limit;
                }

                statement.executeBatch();
                statement.close();
                connection.close();
            }
        }
    }
}
