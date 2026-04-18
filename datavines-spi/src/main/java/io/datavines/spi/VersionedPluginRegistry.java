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

import io.datavines.spi.classloader.PluginClassLoader;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Immutable registry of plugins keyed by logical name and version.
 */
public final class VersionedPluginRegistry<P> {

    private final Map<String, NavigableMap<PluginVersion, P>> registry;
    private final Map<String, PluginClassLoader> classLoaders;
    private final String registryName;

    private VersionedPluginRegistry(
            Map<String, NavigableMap<PluginVersion, P>> registry,
            Map<String, PluginClassLoader> classLoaders,
            String registryName) {
        Map<String, NavigableMap<PluginVersion, P>> immutable = new LinkedHashMap<String, NavigableMap<PluginVersion, P>>();
        for (Map.Entry<String, NavigableMap<PluginVersion, P>> entry : registry.entrySet()) {
            immutable.put(entry.getKey(),
                    Collections.unmodifiableNavigableMap(new TreeMap<PluginVersion, P>(entry.getValue())));
        }
        this.registry = Collections.unmodifiableMap(immutable);
        this.classLoaders = Collections.unmodifiableMap(new LinkedHashMap<String, PluginClassLoader>(classLoaders));
        this.registryName = registryName;
    }

    public P getLatest(String pluginName) {
        NavigableMap<PluginVersion, P> versions = requireVersionMap(pluginName);
        return versions.lastEntry().getValue();
    }

    public P get(String pluginName, String version) {
        return get(pluginName, PluginVersion.of(version));
    }

    public P get(String pluginName, PluginVersion version) {
        NavigableMap<PluginVersion, P> versions = requireVersionMap(pluginName);
        P plugin = versions.get(version);
        if (plugin == null) {
            throw new ProviderNotFoundException(registryName,
                    pluginName + "@" + version, versions.keySet());
        }
        return plugin;
    }

    public P getCompatible(String pluginName, VersionConstraint constraint) {
        NavigableMap<PluginVersion, P> versions = requireVersionMap(pluginName);
        PluginVersion selected = constraint.selectLatest(versions.keySet());
        if (selected == null) {
            throw new ProviderNotFoundException(registryName,
                    pluginName + " matching " + constraint, versions.keySet());
        }
        return versions.get(selected);
    }

    public NavigableMap<PluginVersion, P> getAllVersions(String pluginName) {
        return requireVersionMap(pluginName);
    }

    public boolean supportsPlugin(String pluginName) {
        return registry.containsKey(pluginName);
    }

    public boolean supportsVersion(String pluginName, String version) {
        NavigableMap<PluginVersion, P> versions = registry.get(pluginName);
        return versions != null && versions.containsKey(PluginVersion.of(version));
    }

    public Set<String> supportedPluginNames() {
        return registry.keySet();
    }

    public PluginClassLoader getClassLoader(String pluginId) {
        return classLoaders.get(pluginId);
    }

    public String getRegistryName() {
        return registryName;
    }

    public boolean isEmpty() {
        return registry.isEmpty();
    }

    public static <P> Builder<P> builder(String registryName) {
        return new Builder<P>(registryName);
    }

    public static final class Builder<P> {
        private final String registryName;
        private final Map<String, NavigableMap<PluginVersion, P>> map =
                new LinkedHashMap<String, NavigableMap<PluginVersion, P>>();
        private final Map<String, PluginClassLoader> classLoaders =
                new LinkedHashMap<String, PluginClassLoader>();

        Builder(String registryName) {
            this.registryName = registryName;
        }

        public Builder<P> register(PluginDescriptor descriptor, P plugin,
                                   PluginClassLoader classLoader) {
            return register(descriptor.getPluginName(), descriptor, plugin, classLoader);
        }

        public Builder<P> register(PluginDescriptor descriptor, P plugin) {
            return register(descriptor, plugin, null);
        }

        /**
         * Registers a provider under a logical key, which may differ from {@code plugin.name}.
         */
        public Builder<P> register(String pluginName,
                                   PluginDescriptor descriptor,
                                   P plugin,
                                   PluginClassLoader classLoader) {
            Map<String, P> singleton = new LinkedHashMap<String, P>();
            singleton.put(pluginName, plugin);
            return registerAll(descriptor, singleton, classLoader);
        }

        /**
         * Registers all keys atomically after duplicate checks pass.
         */
        public Builder<P> registerAll(PluginDescriptor descriptor,
                                      Map<String, P> keyedPlugins,
                                      PluginClassLoader classLoader) {
            PluginVersion version = descriptor.getVersion();

            for (Map.Entry<String, P> entry : keyedPlugins.entrySet()) {
                String pluginName = entry.getKey();
                P plugin = entry.getValue();

                NavigableMap<PluginVersion, P> versions = map.get(pluginName);
                if (versions == null) {
                    continue;
                }
                P existing = versions.get(version);
                if (existing != null) {
                    throw new DuplicateProviderException(
                            registryName,
                            pluginName + "@" + version,
                            existing.getClass().getName(),
                            plugin.getClass().getName());
                }
            }

            if (classLoader != null) {
                PluginClassLoader previous = classLoaders.get(descriptor.getPluginId());
                if (previous != null && previous != classLoader) {
                    throw new IllegalStateException(
                            "Duplicate classloader for plugin " + descriptor.getPluginId());
                }
            }

            for (Map.Entry<String, P> entry : keyedPlugins.entrySet()) {
                String pluginName = entry.getKey();
                P plugin = entry.getValue();

                NavigableMap<PluginVersion, P> versions = map.get(pluginName);
                if (versions == null) {
                    versions = new TreeMap<PluginVersion, P>();
                    map.put(pluginName, versions);
                }
                versions.put(version, plugin);
            }

            if (classLoader != null) {
                classLoaders.put(descriptor.getPluginId(), classLoader);
            }

            return this;
        }

        public VersionedPluginRegistry<P> build() {
            return new VersionedPluginRegistry<P>(map, classLoaders, registryName);
        }
    }

    private NavigableMap<PluginVersion, P> requireVersionMap(String pluginName) {
        NavigableMap<PluginVersion, P> versions = registry.get(pluginName);
        if (versions == null || versions.isEmpty()) {
            throw new ProviderNotFoundException(registryName, pluginName, registry.keySet());
        }
        return versions;
    }
}
