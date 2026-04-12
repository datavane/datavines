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
package io.datavines.server.plugin;

import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.api.engine.EngineExecutor;
import io.datavines.engine.config.JobConfigurationBuilder;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.ResultFormula;
import io.datavines.metric.api.SqlMetric;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.spi.PluginBootstrap;
import io.datavines.registry.api.Registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DataVines 插件初始化器。
 *
 * <p>支持两种加载模式，在启动时自动选择：
 *
 * <h3>1. 目录模式（生产环境）</h3>
 * <p>当 {@code plugins/} 目录存在且包含版本化子目录时，使用
 * {@link PluginDirectoryLoader} + {@link PluginDiscoveryBootstrap}，
 * 实现 ClassLoader 隔离，支持多版本同一插件并存。
 * <pre>
 * {deploy.dir}/
 * ├── plugins/
 * │   ├── connector/
 * │   │   └── mysql/
 * │   │       └── 1.0.0-SNAPSHOT/
 * │   │           ├── datavines-connector-mysql-1.0.0-SNAPSHOT.jar
 * │   │           └── mysql-connector-j-*.jar
 * │   └── registry/
 * │       └── mysql/
 * │           └── 1.0.0-SNAPSHOT/
 * │               └── datavines-registry-mysql-1.0.0-SNAPSHOT.jar
 * ├── libs/     (server + common runtime libraries)
 * └── engine/   (spark/flink JARs for job submission)
 * </pre>
 *
 * <h3>2. Classpath 模式（测试 / 特殊开发环境）</h3>
 * <p>当 {@code plugins/} 目录不存在或为空时，退回到
 * {@link ClasspathPluginLoader}：通过 {@link java.util.ServiceLoader} 在
 * 当前 classpath 上扫描所有插件实现，并读取各 JAR 内的
 * {@code META-INF/datavines-plugin.properties} 来获取版本信息。
 * <br>此模式要求插件实现模块也在当前 classpath 上。IntelliJ IDEA 直接运行
 * {@code datavines-server} 时默认只有 server 依赖，通常不会包含各插件实现，
 * 因此推荐在 IDEA 中也显式配置 {@code -Ddatavines.plugins.dir=/absolute/path/to/plugins}
 * 使用目录模式。
 *
 * <h3>插件目录配置</h3>
 * <ul>
 *   <li>System property {@code datavines.plugins.dir}：覆盖默认路径</li>
 *   <li>默认：工作目录下的 {@code plugins/} 子目录</li>
 * </ul>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * // 在 DataVinesServer.initializeAndStart() 的最开始调用
 * DataVinesPluginInitializer.initialize();
 * }</pre>
 */
public final class DataVinesPluginInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataVinesPluginInitializer.class);

    /**
     * System property 用于覆盖 plugins 目录路径。
     * 可在 IDEA Run Configuration 的 VM Options 中设置：
     * {@code -Ddatavines.plugins.dir=/absolute/path/to/plugins}
     */
    public static final String PLUGINS_DIR_PROPERTY = "datavines.plugins.dir";

    /** 默认 plugins 目录名（相对于工作目录）。 */
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
                            SlasHandlerPlugin.class, "notification", SlasHandlerPlugin::getPluginNames),
                    PluginBootstrap.SpiRegistration.of(
                            Registry.class, "registry", Registry::getPluginNames)
            ));

    private DataVinesPluginInitializer() {}

    /**
     * 初始化插件系统。应在任何 {@link io.datavines.spi.PluginDiscovery} 调用之前执行。
     *
     * <p>自动选择加载模式：
     * <ul>
     *   <li>plugins 目录存在且含有版本化子目录 → 目录模式（生产）</li>
     *   <li>plugins 目录不存在或为空 → classpath 模式（IDE/开发）</li>
     * </ul>
     */
    public static void initialize() {
        PluginBootstrap.initialize(
                "DataVines Plugin System",
                PLUGINS_DIR_PROPERTY,
                DEFAULT_PLUGINS_DIR,
                REGISTRATIONS,
                DataVinesPluginInitializer.class.getClassLoader());
    }

    /**
     * 在生产部署目录下创建 plugins 占位说明文件（如果目录不存在则创建）。
     * 在 dist 解包后调用一次，方便运维理解目录用途。
     */
    public static void createPluginsDirIfMissing(File deployDir) {
        File pluginsDir = new File(deployDir, DEFAULT_PLUGINS_DIR);
        if (!pluginsDir.exists() && pluginsDir.mkdirs()) {
            File readme = new File(pluginsDir, "README.txt");
            try (FileWriter fw = new FileWriter(readme)) {
                fw.write("DataVines Versioned Plugin Directory\n");
                fw.write("=====================================\n");
                fw.write("Place versioned plugin JARs in subdirectories:\n\n");
                fw.write("  plugins/{plugin.module}/{plugin.name}/{plugin.version}/*.jar\n\n");
                fw.write("Examples:\n");
                fw.write("  plugins/connector/mysql/1.0.0-SNAPSHOT/datavines-connector-mysql-1.0.0-SNAPSHOT.jar\n");
                fw.write("  plugins/registry/mysql/1.0.0-SNAPSHOT/datavines-registry-mysql-1.0.0-SNAPSHOT.jar\n\n");
                fw.write("Each plugin JAR must contain META-INF/datavines-plugin.properties.\n");
                fw.write("When populated, DataVines uses ClassLoader isolation per version.\n");
                fw.write("When empty, plugins are loaded only from the application classpath.\n");
            } catch (IOException e) {
                log.debug("Could not write plugins/README.txt: {}", e.getMessage());
            }
            log.info("Created plugins directory: {}", pluginsDir.getAbsolutePath());
        }
    }
}
