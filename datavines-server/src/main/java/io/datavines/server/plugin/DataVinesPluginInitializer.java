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
 * Plugin bootstrap entrypoint for the server process.
 */
public final class DataVinesPluginInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataVinesPluginInitializer.class);

    /**
     * Overrides the default plugins directory.
     */
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
                            SlasHandlerPlugin.class, "notification", SlasHandlerPlugin::getPluginNames),
                    PluginBootstrap.SpiRegistration.of(
                            Registry.class, "registry", Registry::getPluginNames)
            ));

    private DataVinesPluginInitializer() {}

    public static void initialize() {
        PluginBootstrap.initialize(
                "DataVines Plugin System",
                PLUGINS_DIR_PROPERTY,
                DEFAULT_PLUGINS_DIR,
                REGISTRATIONS,
                DataVinesPluginInitializer.class.getClassLoader());
    }

    /**
     * Writes a short README into an empty deployment plugins directory.
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
