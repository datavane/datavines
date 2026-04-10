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
package io.datavines.engine.local.core;

import io.datavines.common.config.Config;
import io.datavines.common.config.DataVinesJobConfig;
import io.datavines.engine.api.component.Component;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.core.BaseDataVinesBootstrap;
import io.datavines.engine.core.enums.ConnectorType;
import io.datavines.engine.local.api.LocalSink;
import io.datavines.engine.local.api.LocalSource;
import io.datavines.engine.local.api.LocalRuntimeEnvironment;
import io.datavines.engine.local.api.utils.LoggerFactory;
import io.datavines.engine.local.connector.*;
import io.datavines.engine.local.transform.sql.SqlTransform;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import static io.datavines.engine.api.EngineConstants.PLUGIN_TYPE;
import static io.datavines.engine.api.EngineConstants.TYPE;


public class LocalDataVinesBootstrap extends BaseDataVinesBootstrap {

    public LocalDataVinesBootstrap(Logger logger) {
        LoggerFactory.setLogger(logger);
    }

    @Override
    protected RuntimeEnvironment createRuntimeEnvironment(DataVinesJobConfig config) {
        LocalRuntimeEnvironment runtimeEnvironment = new LocalRuntimeEnvironment();
        Config runtimeConfig = new Config(config.getEnvConfig().getConfig());
        runtimeConfig.put(TYPE, config.getEnvConfig().getType());
        runtimeEnvironment.setConfig(runtimeConfig);
        runtimeEnvironment.prepare();
        return runtimeEnvironment;
    }

    @Override
    protected List<Component> createSources(DataVinesJobConfig config) {
        return config.getSourceParameters().stream().map(sourceConfig -> {
            LocalSource source;
            switch (ConnectorType.of(sourceConfig.getPlugin())) {
                case JDBC:
                    source = new BaseJdbcSource();
                    break;
                case FILE:
                    source = new LocalFileSource();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported local source plugin: " + sourceConfig.getPlugin());
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
            LocalSink sink;
            switch (ConnectorType.of(sinkConfig.getPlugin())) {
                case JDBC:
                    sink = new BaseJdbcSink();
                    break;
                case MYSQL:
                    sink = new MySQLSink();
                    break;
                case FILE:
                    sink = new LocalFileSink();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported local sink plugin: " + sinkConfig.getPlugin());
            }
            sinkConfig.getConfig().put(PLUGIN_TYPE, sinkConfig.getType());
            sink.setConfig(new Config(sinkConfig.getConfig()));
            return (Component) sink;
        }).collect(Collectors.toList());
    }
}
