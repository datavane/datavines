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
package io.datavines.engine.flink.api.stream;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.common.config.ConfigRuntimeException;
import io.datavines.common.exception.DataVinesException;
import io.datavines.engine.api.env.Execution;
import io.datavines.engine.api.plugin.Plugin;
import io.datavines.engine.flink.api.FlinkRuntimeEnvironment;
import io.datavines.engine.flink.api.entity.FLinkColumnInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.List;

import static io.datavines.engine.api.EngineConstants.*;

@Slf4j
public class FlinkStreamExecution implements Execution<FlinkStreamSource, FlinkStreamTransform, FlinkStreamSink>, Plugin {

    private final FlinkRuntimeEnvironment flinkEnv;

    private Config config;

    public FlinkStreamExecution(FlinkRuntimeEnvironment flinkEnv) {
        this.flinkEnv = flinkEnv;
        this.config = new Config();
    }

    @Override
    public void setConfig(Config config) {
        if (config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {
        return new CheckResult(true, "Configuration check passed");
    }

    @Override
    public void prepare() throws Exception {
        // Initialization if needed
    }

    @Override
    public void execute(List<FlinkStreamSource> sources, List<FlinkStreamTransform> transforms, List<FlinkStreamSink> sinks) throws Exception {
        sources.forEach(s -> {
            try {
                registerDatasource(s);
            } catch (Exception e) {
                log.error("register datasource error", e);
                throw new DataVinesException(e);
            }
        });

        if (!sources.isEmpty()) {
            DataStream<Row> ds = null;
            for (FlinkStreamTransform transform : transforms) {
                ds = transform.process(ds, flinkEnv);
                registerTransformTempView(transform, ds);
            }

            for (FlinkStreamSink sink: sinks) {
                sink.output(ds, flinkEnv);
            }
        }

        flinkEnv.getEnv().execute();
    }

    @Override
    public void stop() throws Exception {
        // Flink's execution doesn't need explicit stopping
    }

    private void createTemporaryView(String tableName, DataStream<Row> dataStream) {
        StreamTableEnvironment tableEnv = flinkEnv.getTableEnv();
        tableEnv.createTemporaryView(tableName, dataStream);
    }

    private void registerDatasource(FlinkStreamSource source) throws Exception {
        Config conf = source.getConfig();
        if (conf.has(OUTPUT_TABLE)) {
            source.getData(flinkEnv);
        } else {
            throw new ConfigRuntimeException(
                    "Plugin[" + source.getClass().getName() + "] must be registered as dataset/table, please set \"result_table_name\" config");
        }
    }

    private void registerTransformTempView(FlinkStreamTransform transform, DataStream<Row> ds) {
        Config config = transform.getConfig();
        if (config.has(OUTPUT_TABLE)) {
            String tableName = config.getString(OUTPUT_TABLE);
            createTemporaryView(tableName, ds);
        }
    }
}
