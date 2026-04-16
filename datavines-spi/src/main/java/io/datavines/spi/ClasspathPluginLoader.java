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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Loads plugins from the application classpath.
 *
 * <p>This mode keeps version metadata but does not provide classloader isolation.
 */
public final class ClasspathPluginLoader {

    private static final Logger log = LoggerFactory.getLogger(ClasspathPluginLoader.class);

    private static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";

    private final ClassLoader classLoader;

    public ClasspathPluginLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClasspathPluginLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public <P> VersionedPluginRegistry<P> load(Class<P> serviceType, Function<P, String> keyExtractor) {
        DescriptorIndex descriptorIndex = scanDescriptors();
        List<P> providers = ServiceLoaderUtils.loadAll(serviceType, classLoader);

        VersionedPluginRegistry.Builder<P> builder =
                VersionedPluginRegistry.builder(serviceType.getSimpleName());

        for (P provider : providers) {
            String pluginName = keyExtractor.apply(provider);
            if (pluginName == null) {
                log.warn("Provider {} returned null plugin name, skipping",
                        provider.getClass().getName());
                continue;
            }

            PluginDescriptor descriptor = descriptorIndex.findDescriptor(pluginName, provider);
            registerSafely(builder, descriptor, provider);
        }

        VersionedPluginRegistry<P> registry = builder.build();
        log.info("ClasspathPluginLoader loaded {} plugins with {} total versions for {}",
                registry.supportedPluginNames().size(),
                countTotalVersions(registry),
                serviceType.getSimpleName());
        return registry;
    }

    public <P> VersionedPluginRegistry<P> loadMultiKey(Class<P> serviceType,
                                                        Function<P, Collection<String>> keysExtractor) {
        DescriptorIndex descriptorIndex = scanDescriptors();
        List<P> providers = ServiceLoaderUtils.loadAll(serviceType, classLoader);

        VersionedPluginRegistry.Builder<P> builder =
                VersionedPluginRegistry.builder(serviceType.getSimpleName());

        for (P provider : providers) {
            Collection<String> pluginNames = keysExtractor.apply(provider);
            if (pluginNames == null || pluginNames.isEmpty()) {
                log.warn("Provider {} returned empty plugin names, skipping",
                        provider.getClass().getName());
                continue;
            }

            for (String pluginName : pluginNames) {
                if (pluginName == null || pluginName.trim().isEmpty()) {
                    log.warn("Provider {} returned blank plugin name, skipping",
                            provider.getClass().getName());
                    continue;
                }

                String normalizedName = pluginName.trim();
                PluginDescriptor descriptor = descriptorIndex.findDescriptor(normalizedName, provider);
                registerSafely(builder, normalizedName, descriptor, provider);
            }
        }

        VersionedPluginRegistry<P> registry = builder.build();
        log.info("ClasspathPluginLoader loaded {} plugins for {}",
                registry.supportedPluginNames().size(), serviceType.getSimpleName());
        return registry;
    }

    /**
     * Matches providers to descriptors by logical name first, then by class origin.
     */
    static final class DescriptorIndex {
        private final Map<String, PluginDescriptor> byName;
        private final Map<String, PluginDescriptor> byUrlPrefix;

        DescriptorIndex(Map<String, PluginDescriptor> byName,
                        Map<String, PluginDescriptor> byUrlPrefix) {
            this.byName = byName;
            this.byUrlPrefix = byUrlPrefix;
        }

        <P> PluginDescriptor findDescriptor(String pluginName, P provider) {
            PluginDescriptor descriptor = byName.get(pluginName);
            if (descriptor != null) {
                return descriptor;
            }

            descriptor = findByClassOrigin(provider);
            if (descriptor != null) {
                log.debug("Matched plugin '{}' to descriptor {} by class origin",
                        pluginName, descriptor.getPluginId());
                return descriptor;
            }

            log.debug("No descriptor found for plugin '{}' (class: {}), using default version {}",
                    pluginName, provider.getClass().getName(), DEFAULT_VERSION);
            return PluginDescriptor.of(pluginName, DEFAULT_VERSION);
        }

        private <P> PluginDescriptor findByClassOrigin(P provider) {
            String classOrigin = getCodeSourceUrl(provider.getClass());
            if (classOrigin == null) {
                return null;
            }
            for (Map.Entry<String, PluginDescriptor> entry : byUrlPrefix.entrySet()) {
                if (entry.getKey().startsWith(classOrigin)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        private static String getCodeSourceUrl(Class<?> clazz) {
            try {
                ProtectionDomain pd = clazz.getProtectionDomain();
                if (pd == null) {
                    return null;
                }
                CodeSource cs = pd.getCodeSource();
                if (cs == null || cs.getLocation() == null) {
                    return null;
                }
                return cs.getLocation().toString();
            } catch (SecurityException e) {
                return null;
            }
        }
    }

    private DescriptorIndex scanDescriptors() {
        Map<String, PluginDescriptor> byName = new LinkedHashMap<String, PluginDescriptor>();
        Map<String, PluginDescriptor> byUrlPrefix = new LinkedHashMap<String, PluginDescriptor>();
        try {
            Enumeration<URL> resources = classLoader.getResources(PluginDescriptor.DESCRIPTOR_PATH);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream is = url.openStream()) {
                    Properties props = new Properties();
                    props.load(is);

                    String name = props.getProperty("plugin.name");
                    String version = props.getProperty("plugin.version");

                    if (name == null || name.trim().isEmpty()) {
                        log.debug("Skipping descriptor without plugin.name: {}", url);
                        continue;
                    }
                    if (version == null || version.trim().isEmpty()) {
                        version = DEFAULT_VERSION;
                    }

                    String spiVersion = props.getProperty("plugin.spi.version", "0.0.0");
                    String mainRange = props.getProperty("plugin.main.version.range", "");
                    String description = props.getProperty("plugin.description", "");
                    String module = props.getProperty("plugin.module", "");

                    PluginDescriptor desc = PluginDescriptor.of(
                            name.trim(), module.trim(), version.trim(),
                            spiVersion.trim(),
                            mainRange.trim(), description.trim());

                    byName.put(name.trim(), desc);
                    byUrlPrefix.put(url.toString(), desc);
                    log.debug("Scanned descriptor: {} from {}", desc.getPluginId(), url);

                } catch (Exception e) {
                    log.warn("Failed to parse descriptor from {}: {}", url, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan for plugin descriptors: {}", e.getMessage());
        }
        log.debug("Scanned {} plugin descriptors from classpath", byName.size());
        return new DescriptorIndex(byName, byUrlPrefix);
    }

    private <P> void registerSafely(VersionedPluginRegistry.Builder<P> builder,
                                     PluginDescriptor descriptor, P plugin) {
        try {
            builder.register(descriptor, plugin, null);
        } catch (DuplicateProviderException e) {
            log.debug("Skipping duplicate registration for {}: {}",
                    descriptor.getPluginId(), e.getMessage());
        }
    }

    private <P> void registerSafely(VersionedPluginRegistry.Builder<P> builder,
                                    String pluginName,
                                    PluginDescriptor descriptor,
                                    P plugin) {
        try {
            builder.register(pluginName, descriptor, plugin, null);
        } catch (DuplicateProviderException e) {
            log.debug("Skipping duplicate registration for {} using logical key '{}': {}",
                    descriptor.getPluginId(), pluginName, e.getMessage());
        }
    }

    private <P> int countTotalVersions(VersionedPluginRegistry<P> registry) {
        int count = 0;
        for (String name : registry.supportedPluginNames()) {
            count += registry.getAllVersions(name).size();
        }
        return count;
    }
}
