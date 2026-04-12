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
package io.datavines.runner.plugin;

import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.api.engine.EngineExecutor;
import io.datavines.engine.config.JobConfigurationBuilder;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.ResultFormula;
import io.datavines.metric.api.SqlMetric;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.spi.PluginBootstrap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Runner 进程专用插件初始化器。
 *
 * <p>Runner 没有依赖 server 模块，因此需要在本模块中声明所需的 SPI 注册表。
 * 启动逻辑仍复用 {@link PluginBootstrap}，确保目录模式语义与 server 一致。
 */
public final class RunnerPluginInitializer {

    public static final String PLUGINS_DIR_PROPERTY = "datavines.plugins.dir";

    private static final String DEFAULT_PLUGINS_DIR = "plugins";

    private static final List<PluginBootstrap.SpiRegistration<?>> REGISTRATIONS =
            Collections.unmodifiableList(Arrays.<PluginBootstrap.SpiRegistration<?>>asList(
                    PluginBootstrap.SpiRegistration.of(
                            ConnectorFactory.class, "connector", ConnectorFactory::getPluginNames),
                    PluginBootstrap.SpiRegistration.of(
                            SqlMetric.class, "metric", SqlMetric::getPluginNames),
                    PluginBootstrap.SpiRegistration.of(
                            ExpectedValue.class, "expected-value", ExpectedValue::getPluginNames),
                    PluginBootstrap.SpiRegistration.of(
                            ResultFormula.class, "result-formula", ResultFormula::getPluginNames),
                    PluginBootstrap.SpiRegistration.of(
                            JobConfigurationBuilder.class, "engine", JobConfigurationBuilder::getPluginNames),
                    PluginBootstrap.SpiRegistration.of(
                            EngineExecutor.class, "engine", EngineExecutor::getPluginNames),
                    PluginBootstrap.SpiRegistration.of(
                            SlasHandlerPlugin.class, "notification", SlasHandlerPlugin::getPluginNames)
            ));

    private RunnerPluginInitializer() {
    }

    public static void initialize() {
        PluginBootstrap.initialize(
                "DataVines Runner Plugin System",
                PLUGINS_DIR_PROPERTY,
                DEFAULT_PLUGINS_DIR,
                REGISTRATIONS,
                RunnerPluginInitializer.class.getClassLoader());
    }
}
