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
package io.datavines.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Bootstraps plugin discovery from either the plugins directory or the classpath.
 */
public final class PluginBootstrap {

    private static final Logger log = LoggerFactory.getLogger(PluginBootstrap.class);

    private PluginBootstrap() {
    }

    public static void initialize(String bootstrapName,
                                  String pluginsDirProperty,
                                  String defaultPluginsDir,
                                  List<SpiRegistration<?>> registrations,
                                  ClassLoader spiClassLoader) {
        if (PluginDiscoveryBootstrap.isInitialized()) {
            log.debug("PluginDiscoveryBootstrap already initialized, skipping {} bootstrap.", bootstrapName);
            return;
        }

        File pluginsDir = resolvePluginsDir(pluginsDirProperty, defaultPluginsDir);
        if (pluginsDir != null && pluginsDir.isDirectory() && hasVersionedSubdirs(pluginsDir)) {
            initializeFromDirectory(bootstrapName, pluginsDir, registrations, spiClassLoader);
        } else {
            logClasspathMode(bootstrapName, pluginsDir, pluginsDirProperty, defaultPluginsDir);
        }
    }

    private static void initializeFromDirectory(String bootstrapName,
                                                File pluginsDir,
                                                List<SpiRegistration<?>> registrations,
                                                ClassLoader spiClassLoader) {
        log.info("========================================================");
        log.info("{} - DIRECTORY MODE", bootstrapName);
        log.info("  Plugins directory: {}", pluginsDir.getAbsolutePath());
        log.info("========================================================");

        Map<Class<?>, VersionedPluginRegistry<?>> registries =
                new HashMap<Class<?>, VersionedPluginRegistry<?>>();

        for (SpiRegistration<?> registration : registrations) {
            File moduleDir = new File(pluginsDir, registration.getModuleName());
            if (!moduleDir.isDirectory()) {
                log.debug("  Module dir not found, skipping: {}", moduleDir.getAbsolutePath());
                continue;
            }

            PluginDirectoryLoader loader = new PluginDirectoryLoader(
                    Collections.singletonList(moduleDir.toPath()), spiClassLoader);

            try {
                VersionedPluginRegistry<?> registry = registration.load(loader);
                if (!registry.isEmpty()) {
                    registries.put(registration.getSpiType(), registry);
                    log.info("  [{}] Loaded {} plugin key(s) for SPI: {}",
                            registration.getModuleName(),
                            registry.supportedPluginNames().size(),
                            registration.getSpiType().getSimpleName());
                } else {
                    log.debug("  [{}] No plugins found for SPI: {}",
                            registration.getModuleName(), registration.getSpiType().getSimpleName());
                }
            } catch (Exception e) {
                log.warn("  [{}] Failed to load SPI {} from '{}': {}",
                        registration.getModuleName(),
                        registration.getSpiType().getSimpleName(),
                        moduleDir.getAbsolutePath(),
                        e.getMessage(), e);
            }
        }

        if (registries.isEmpty()) {
            log.warn("No versioned plugins found in '{}'. Falling back to classpath mode.", pluginsDir);
            logClasspathMode(bootstrapName, pluginsDir, null, null);
            return;
        }

        PluginDiscoveryBootstrap.initialize(registries);
        log.info("Plugin system initialized: {} SPI type(s) with versioned plugins.", registries.size());
    }

    public static File resolvePluginsDir(String pluginsDirProperty, String defaultPluginsDir) {
        String dirPath = pluginsDirProperty != null ? System.getProperty(pluginsDirProperty) : null;
        if (dirPath != null && !dirPath.trim().isEmpty()) {
            File dir = new File(dirPath.trim());
            log.debug("Using plugins dir from system property '{}': {}",
                    pluginsDirProperty, dir.getAbsolutePath());
            return dir;
        }
        return new File(System.getProperty("user.dir"), defaultPluginsDir);
    }

    public static boolean hasVersionedSubdirs(File pluginsDir) {
        File[] level1Dirs = pluginsDir.listFiles(File::isDirectory);
        if (level1Dirs == null || level1Dirs.length == 0) {
            return false;
        }

        for (File level1 : level1Dirs) {
            File[] level2Dirs = level1.listFiles(File::isDirectory);
            if (level2Dirs == null) {
                continue;
            }
            for (File level2 : level2Dirs) {
                File[] level3Dirs = level2.listFiles(File::isDirectory);
                if (level3Dirs != null && level3Dirs.length > 0) {
                    return true;
                }
                File[] jars = level2.listFiles(f -> f.getName().endsWith(".jar"));
                if (jars != null && jars.length > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void logClasspathMode(String bootstrapName,
                                         File pluginsDir,
                                         String pluginsDirProperty,
                                         String defaultPluginsDir) {
        log.info("========================================================");
        log.info("{} - CLASSPATH MODE", bootstrapName);
        if (pluginsDir == null || !pluginsDir.isDirectory()) {
            log.info("  Reason: plugins directory not found at '{}'",
                    pluginsDir != null ? pluginsDir.getAbsolutePath() : defaultPluginsDir);
        } else {
            log.info("  Reason: plugins directory exists but has no versioned subdirectories.");
        }
        log.info("  All plugins will be discovered via ServiceLoader on the classpath.");
        log.info("  Classpath mode requires plugin implementation modules/JARs on the application classpath.");
        log.info("  To use directory mode: create plugins/{{module}}/{{name}}/{{version}}/*.jar structure");
        if (pluginsDirProperty != null) {
            log.info("  Or set -D{}=/path/to/plugins", pluginsDirProperty);
        }
        log.info("========================================================");
    }

    public static final class SpiRegistration<P> {
        private final Class<P> spiType;
        private final String moduleName;
        private final Function<P, Collection<String>> keysExtractor;

        private SpiRegistration(Class<P> spiType,
                                String moduleName,
                                Function<P, Collection<String>> keysExtractor) {
            this.spiType = spiType;
            this.moduleName = moduleName;
            this.keysExtractor = keysExtractor;
        }

        public static <P> SpiRegistration<P> of(Class<P> spiType,
                                                String moduleName,
                                                Function<P, Collection<String>> keysExtractor) {
            return new SpiRegistration<P>(spiType, moduleName, keysExtractor);
        }

        VersionedPluginRegistry<P> load(PluginDirectoryLoader loader) {
            return loader.loadMultiKey(spiType, keysExtractor);
        }

        public Class<P> getSpiType() {
            return spiType;
        }

        public String getModuleName() {
            return moduleName;
        }
    }
}
