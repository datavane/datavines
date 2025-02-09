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
package io.datavines.engine.flink.jdbc.sink;

import io.datavines.engine.flink.api.entity.FLinkColumnInfo;
import io.datavines.engine.flink.jdbc.utils.FlinkTableUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.types.Row;

import io.datavines.common.config.Config;
import io.datavines.common.config.CheckResult;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.flink.api.FlinkRuntimeEnvironment;
import io.datavines.engine.flink.api.stream.FlinkStreamSink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.datavines.common.ConfigConstants.*;
import static io.datavines.common.ConfigConstants.PASSWORD;

@Slf4j
public class JdbcSink implements FlinkStreamSink {

    private Config config = new Config();

    private final List<FLinkColumnInfo> columns = new ArrayList<>();

    @Override
    public void output(DataStream<Row> dataStream, FlinkRuntimeEnvironment environment) {
        String sql = config.getString(SQL).replace("\\n", " ").replaceAll("/", "");
        Table table = environment.getTableEnv().sqlQuery(sql);
        ResolvedSchema schema = table.getResolvedSchema();
        schema.getColumns().forEach(column -> {
            FLinkColumnInfo columnInfo = new FLinkColumnInfo();
            columnInfo.setColumn(column.getName());
            columnInfo.setDataType(column.getDataType().getLogicalType().asSerializableString());
            columns.add(columnInfo);
        });
        checkTableNotExistAndCreate();

        String createTableSql = FlinkTableUtils.generateCreateTableStatement(config.getString(OUTPUT_TABLE), config.getString(TABLE), columns, config);
        log.info("sink create table sql: {}", createTableSql);
        environment.getTableEnv().executeSql(createTableSql);
        table.executeInsert(config.getString(OUTPUT_TABLE));
    }

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
        List<String> requiredOptions = Arrays.asList(URL, TABLE, USER, PASSWORD);

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
    public void prepare(RuntimeEnvironment env) throws Exception {
    }

    private void checkTableNotExistAndCreate() {
        // Check if the table exists
        // If not, create the table
    }
}
