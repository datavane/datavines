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
package io.datavines.engine.flink.core;

import io.datavines.common.config.Config;
import io.datavines.common.config.DataVinesJobConfig;
import io.datavines.engine.api.component.Component;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.core.BaseDataVinesBootstrap;
import io.datavines.engine.core.enums.ConnectorType;
import io.datavines.engine.flink.api.FlinkRuntimeEnvironment;
import io.datavines.engine.flink.api.stream.FlinkStreamSink;
import io.datavines.engine.flink.api.stream.FlinkStreamSource;
import io.datavines.engine.flink.jdbc.sink.JdbcSink;
import io.datavines.engine.flink.jdbc.sink.MySQLSink;
import io.datavines.engine.flink.jdbc.source.JdbcSource;
import io.datavines.engine.flink.transform.sql.SqlTransform;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static io.datavines.engine.api.EngineConstants.PLUGIN_TYPE;
import static io.datavines.engine.api.EngineConstants.TYPE;

@Slf4j
public class FlinkDataVinesBootstrap extends BaseDataVinesBootstrap {

    public static void main(String[] args) {
        FlinkDataVinesBootstrap bootstrap = new FlinkDataVinesBootstrap();

        if (args.length == 1) {
            String arg = args[0];
            args[0] = new String(Base64.getDecoder().decode(arg));
            bootstrap.execute(args);
        }
    }

    @Override
    protected RuntimeEnvironment createRuntimeEnvironment(DataVinesJobConfig config) {
        FlinkRuntimeEnvironment runtimeEnvironment = new FlinkRuntimeEnvironment();
        Config runtimeConfig = new Config(config.getEnvConfig().getConfig());
        runtimeConfig.put(TYPE, config.getEnvConfig().getType());
        runtimeEnvironment.setConfig(runtimeConfig);
        runtimeEnvironment.prepare();
        return runtimeEnvironment;
    }

    @Override
    protected List<Component> createSources(DataVinesJobConfig config) {
        return config.getSourceParameters().stream().map(sourceConfig -> {
            FlinkStreamSource source;
            switch (ConnectorType.of(sourceConfig.getPlugin())) {
                case JDBC:
                    source = new JdbcSource();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported flink source plugin: " + sourceConfig.getPlugin());
            }
            sourceConfig.getConfig().put(PLUGIN_TYPE, sourceConfig.getType());
            source.setConfig(new Config(sourceConfig.getConfig()));
            return (Component) source;
        }).collect(Collectors.toList());
    }

    @Override
    protected List<Component> createTransforms(DataVinesJobConfig config) {
        return config.getTransformParameters().stream().map(transformConfig -> {
            SqlTransform transform = new SqlTransform();
            transformConfig.getConfig().put(PLUGIN_TYPE, transformConfig.getType());
            transform.setConfig(new Config(transformConfig.getConfig()));
            return (Component) transform;
        }).collect(Collectors.toList());
    }

    @Override
    protected List<Component> createSinks(DataVinesJobConfig config) {
        return config.getSinkParameters().stream().map(sinkConfig -> {
            FlinkStreamSink sink;
            switch (ConnectorType.of(sinkConfig.getPlugin())) {
                case JDBC:
                    sink = new JdbcSink();
                    break;
                case MYSQL:
                    sink = new MySQLSink();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported flink sink plugin: " + sinkConfig.getPlugin());
            }
            sinkConfig.getConfig().put(PLUGIN_TYPE, sinkConfig.getType());
            sink.setConfig(new Config(sinkConfig.getConfig()));
            return (Component) sink;
        }).collect(Collectors.toList());
    }
}
