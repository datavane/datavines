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
package io.datavines.engine.flink.transform.sql;

import io.datavines.engine.flink.api.entity.FLinkColumnInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.types.Row;

import io.datavines.common.config.Config;
import io.datavines.common.config.CheckResult;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.flink.api.FlinkRuntimeEnvironment;
import io.datavines.engine.flink.api.stream.FlinkStreamTransform;

import java.util.ArrayList;
import java.util.List;

import static io.datavines.common.ConfigConstants.SQL;

public class SqlTransform implements FlinkStreamTransform {
    
    private Config config = new Config();

    private final List<FLinkColumnInfo> columns = new ArrayList<>();
    
    @Override
    public DataStream<Row> process(DataStream<Row> dataStream, FlinkRuntimeEnvironment environment) {
        StreamTableEnvironment tableEnv = environment.getTableEnv();
        Table resultTable = tableEnv.sqlQuery(config.getString(SQL));

        ResolvedSchema schema = resultTable.getResolvedSchema();
        schema.getColumns().forEach(column -> {
            FLinkColumnInfo columnInfo = new FLinkColumnInfo();
            columnInfo.setColumn(column.getName());
            columnInfo.setDataType(column.getDataType().getLogicalType().asSummaryString());
            columns.add(columnInfo);
        });
        return tableEnv.toDataStream(resultTable);
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
        if (config.has(SQL)) {
            return new CheckResult(true, "");
        } else {
            return new CheckResult(false, "please specify [sql]");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment env) throws Exception {
        // No preparation needed
    }

    @Override
    public List<FLinkColumnInfo> getOutputColumns() {
        return columns;
    }
}
