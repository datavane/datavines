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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;

/**
 * Compatibility layer that exposes versioned registries through the old discovery API.
 */
public final class VersionedPluginDiscovery<T> {

    private final VersionedPluginRegistry<T> registry;
    private final CachingFactory<String, T> latestCache;

    private VersionedPluginDiscovery(VersionedPluginRegistry<T> registry) {
        this.registry = registry;
        this.latestCache = new CachingFactory<String, T>(new LatestVersionCreator<T>(registry));
    }

    public static <T> VersionedPluginDiscovery<T> of(VersionedPluginRegistry<T> registry) {
        return new VersionedPluginDiscovery<T>(registry);
    }

    public T getOrCreatePlugin(String name) {
        return latestCache.get(name);
    }

    public T getOrCreatePlugin(String name, String version) {
        return registry.get(name, version);
    }

    public T getOrCreatePlugin(String name, VersionConstraint constraint) {
        return registry.getCompatible(name, constraint);
    }

    public T getLoadedPlugin(String name) {
        return latestCache.getIfLoaded(name);
    }

    public T getNewPlugin(String name) {
        T provider = registry.getLatest(name);
        try {
            @SuppressWarnings("unchecked")
            T newInstance = (T) provider.getClass().newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Plugin instance (name: " + name + ", class: " + provider.getClass().getName()
                    + ") couldn't be instantiated: " + e.getMessage(), e);
        }
    }

    public boolean hasPlugin(String name) {
        return registry.supportsPlugin(name);
    }

    public boolean hasPlugin(String name, String version) {
        return registry.supportsVersion(name, version);
    }

    public Set<String> getSupportedPlugins() {
        return registry.supportedPluginNames();
    }

    public NavigableMap<PluginVersion, T> getAllVersions(String name) {
        return registry.getAllVersions(name);
    }

    public Set<String> getLoadedPlugins() {
        return latestCache.loadedKeys();
    }

    public Set<T> getSupportedPluginInstances() {
        Set<T> instances = new LinkedHashSet<T>();
        for (String name : getSupportedPlugins()) {
            instances.add(getOrCreatePlugin(name));
        }
        return Collections.unmodifiableSet(instances);
    }

    public VersionedPluginRegistry<T> getRegistry() {
        return registry;
    }

    /**
     * Named class keeps Java 8 compatibility and makes debugging easier than a lambda.
     */
    private static final class LatestVersionCreator<T> implements java.util.function.Function<String, T> {
        private final VersionedPluginRegistry<T> registry;

        LatestVersionCreator(VersionedPluginRegistry<T> registry) {
            this.registry = registry;
        }

        @Override
        public T apply(String name) {
            return registry.getLatest(name);
        }
    }
}
