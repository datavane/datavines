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
import io.datavines.engine.config.JobConfigurationBuilder;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.ResultFormula;
import io.datavines.metric.api.SqlMetric;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.registry.api.Registry;
import io.datavines.spi.ClasspathPluginLoader;
import io.datavines.spi.PluginDescriptor;
import io.datavines.spi.PluginDirectoryLoader;
import io.datavines.spi.PluginDiscoveryBootstrap;
import io.datavines.spi.VersionedPluginRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * │   ├── mysql/
 * │   │   ├── 5.7.44/
 * │   │   │   └── datavines-connector-mysql-5.7.44.jar
 * │   │   └── 8.0.33/
 * │   │       └── datavines-connector-mysql-8.0.33.jar
 * │   └── postgresql/
 * │       └── 42.7.0/
 * │           └── datavines-connector-postgresql.jar
 * ├── libs/     (server + all built-in plugins)
 * └── engine/   (spark/flink JARs for job submission)
 * </pre>
 *
 * <h3>2. Classpath 模式（IDE / 开发环境）</h3>
 * <p>当 {@code plugins/} 目录不存在或为空时，退回到
 * {@link ClasspathPluginLoader}：通过 {@link java.util.ServiceLoader} 在
 * 当前 classpath 上扫描所有插件实现，并读取各 JAR 内的
 * {@code META-INF/datavines-plugin.properties} 来获取版本信息。
 * <br>此模式下，在 IntelliJ IDEA 中直接运行 DataVinesServer 时，
 * 所有插件均可被发现和加载，无需额外配置。
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

    /**
     * 服务端所有 SPI 接口类型列表。
     * {@link PluginDirectoryLoader} 将对每种类型在 plugins 目录中搜索实现。
     */
    private static final List<Class<?>> KNOWN_SERVER_SPI_TYPES = Collections.unmodifiableList(
            Arrays.<Class<?>>asList(
                    ConnectorFactory.class,
                    SqlMetric.class,
                    ExpectedValue.class,
                    ResultFormula.class,
                    JobConfigurationBuilder.class,
                    SlasHandlerPlugin.class,
                    Registry.class
            )
    );

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
        if (PluginDiscoveryBootstrap.isInitialized()) {
            log.debug("PluginDiscoveryBootstrap already initialized, skipping.");
            return;
        }

        File pluginsDir = resolvePluginsDir();

        if (pluginsDir != null && pluginsDir.isDirectory() && hasVersionedSubdirs(pluginsDir)) {
            initializeFromDirectory(pluginsDir);
        } else {
            logClasspathMode(pluginsDir);
            // ClasspathPluginLoader (tier 2) in PluginDiscovery will handle loading automatically.
            // No explicit initialization needed — it activates lazily on first PluginDiscovery call.
        }
    }

    /**
     * 目录模式：使用 PluginDirectoryLoader 加载各版本插件并注册到 PluginDiscoveryBootstrap。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void initializeFromDirectory(File pluginsDir) {
        log.info("========================================================");
        log.info("DataVines Plugin System - DIRECTORY MODE (production)");
        log.info("  Plugins directory: {}", pluginsDir.getAbsolutePath());
        log.info("========================================================");

        ClassLoader spiClassLoader = DataVinesPluginInitializer.class.getClassLoader();
        PluginDirectoryLoader loader = new PluginDirectoryLoader(
                Collections.singletonList(pluginsDir.toPath()),
                spiClassLoader
        );

        Map<Class<?>, VersionedPluginRegistry<?>> registries =
                new HashMap<Class<?>, VersionedPluginRegistry<?>>();

        for (Class<?> spiType : KNOWN_SERVER_SPI_TYPES) {
            try {
                VersionedPluginRegistry registry = loader.load(spiType);
                if (!registry.isEmpty()) {
                    registries.put(spiType, registry);
                    log.info("  Loaded {} plugin(s) for SPI: {}",
                            registry.supportedPluginNames().size(), spiType.getSimpleName());
                }
            } catch (Exception e) {
                log.warn("  Failed to load SPI {} from plugins directory: {}",
                        spiType.getSimpleName(), e.getMessage());
            }
        }

        if (registries.isEmpty()) {
            log.warn("No versioned plugins found in '{}'. Falling back to classpath mode.",
                    pluginsDir);
            logClasspathMode(pluginsDir);
        } else {
            PluginDiscoveryBootstrap.initialize(registries);
            log.info("Plugin system initialized: {} SPI type(s) with versioned plugins.",
                    registries.size());
        }
    }

    /**
     * 解析 plugins 目录。优先使用系统属性，否则使用工作目录下的默认值。
     */
    private static File resolvePluginsDir() {
        String dirPath = System.getProperty(PLUGINS_DIR_PROPERTY);
        if (dirPath != null && !dirPath.trim().isEmpty()) {
            File dir = new File(dirPath.trim());
            log.debug("Using plugins dir from system property '{}': {}",
                    PLUGINS_DIR_PROPERTY, dir.getAbsolutePath());
            return dir;
        }

        // Default: {working dir}/plugins/
        return new File(System.getProperty("user.dir"), DEFAULT_PLUGINS_DIR);
    }

    /**
     * 检查目录下是否含有「版本化」的子目录（即 plugins/{name}/{version}/ 结构）。
     * 仅检查两层深度；如果至少有一个版本目录则返回 true。
     */
    private static boolean hasVersionedSubdirs(File pluginsDir) {
        File[] nameDirs = pluginsDir.listFiles(File::isDirectory);
        if (nameDirs == null || nameDirs.length == 0) {
            return false;
        }
        for (File nameDir : nameDirs) {
            File[] versionDirs = nameDir.listFiles(File::isDirectory);
            if (versionDirs != null && versionDirs.length > 0) {
                return true;
            }
        }
        return false;
    }

    private static void logClasspathMode(File pluginsDir) {
        log.info("========================================================");
        log.info("DataVines Plugin System - CLASSPATH MODE (IDE/development)");
        if (pluginsDir == null || !pluginsDir.isDirectory()) {
            log.info("  Reason: plugins directory not found at '{}'",
                    pluginsDir != null ? pluginsDir.getAbsolutePath() : DEFAULT_PLUGINS_DIR);
        } else {
            log.info("  Reason: plugins directory exists but has no versioned subdirectories.");
        }
        log.info("  All plugins will be discovered via ServiceLoader on the classpath.");
        log.info("  To use directory mode: create plugins/{{name}}/{{version}}/*.jar structure");
        log.info("  Or set -D{}=/path/to/plugins", PLUGINS_DIR_PROPERTY);
        log.info("========================================================");
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
                fw.write("  plugins/{plugin-name}/{version}/*.jar\n\n");
                fw.write("Example (two versions of MySQL connector):\n");
                fw.write("  plugins/mysql/5.7.44/datavines-connector-mysql.jar\n");
                fw.write("  plugins/mysql/8.0.33/datavines-connector-mysql.jar\n\n");
                fw.write("Each plugin JAR must contain META-INF/datavines-plugin.properties.\n");
                fw.write("When populated, DataVines uses ClassLoader isolation per version.\n");
                fw.write("When empty, all plugins are loaded from libs/ on the classpath.\n");
            } catch (IOException e) {
                log.debug("Could not write plugins/README.txt: {}", e.getMessage());
            }
            log.info("Created plugins directory: {}", pluginsDir.getAbsolutePath());
        }
    }
}
