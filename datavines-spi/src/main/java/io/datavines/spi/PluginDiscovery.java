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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backward-compatible facade over versioned and legacy plugin registries.
 */
public final class PluginDiscovery<T> {

    private static final Logger log = LoggerFactory.getLogger(PluginDiscovery.class);

    private static final ConcurrentHashMap<Class<?>, PluginDiscovery<?>> DISCOVERIES = new ConcurrentHashMap<>();

    private final Class<T> type;
    private final KeyedRegistry<String, T> registry;
    private final CachingFactory<String, T> singletonFactory;
    /**
     * -- GETTER --
     *  Exposes the versioned facade when this discovery is backed by a versioned registry.
     */
    @Getter
    private final VersionedPluginDiscovery<T> versionedDiscovery; // nullable

    private PluginDiscovery(Class<T> type, KeyedRegistry<String, T> registry) {
        this.type = type;
        this.registry = registry;
        this.singletonFactory = new CachingFactory<>(registry::get);
        this.versionedDiscovery = null;
    }

    private PluginDiscovery(Class<T> type, VersionedPluginDiscovery<T> versionedDiscovery) {
        this.type = type;
        this.registry = null;
        this.singletonFactory = null;
        this.versionedDiscovery = versionedDiscovery;
    }

    /**
     * Uses bootstrap registries first, then classpath metadata, then legacy ServiceLoader discovery.
     */
    @SuppressWarnings("unchecked")
    public static <T> PluginDiscovery<T> getPluginDiscovery(Class<T> type, Function<T, String> keyExtractor) {
        return (PluginDiscovery<T>) DISCOVERIES.compute(type,
            (k, existing) -> resolveSingleKeyDiscovery(type, keyExtractor, existing));
    }

    /**
     * Multi-key variant of {@link #getPluginDiscovery(Class, Function)}.
     */
    @SuppressWarnings("unchecked")
    public static <T> PluginDiscovery<T> getMultiKeyPluginDiscovery(Class<T> type, Function<T, Collection<String>> keysExtractor) {
        return (PluginDiscovery<T>) DISCOVERIES.compute(type,
            (k, existing) -> resolveMultiKeyDiscovery(type, keysExtractor, existing));
    }

    public T getOrCreatePlugin(String name) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getOrCreatePlugin(name);
        }
        return singletonFactory.get(name);
    }

    public T getOrCreatePlugin(String name, String version) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getOrCreatePlugin(name, version);
        }
        return singletonFactory.get(name);
    }

    public T getOrCreatePlugin(String name, VersionConstraint constraint) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getOrCreatePlugin(name, constraint);
        }
        return singletonFactory.get(name);
    }

    public T getNewPlugin(String name) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getNewPlugin(name);
        }
        T provider = registry.get(name);
        try {
            @SuppressWarnings("unchecked")
            T newInstance = (T) provider.getClass().newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new IllegalStateException(
                "Plugin instance (name: " + name + ", class: " + type.getName()
                + ") couldn't be instantiated: " + e.getMessage(), e);
        }
    }

    public T getLoadedPlugin(String name) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getLoadedPlugin(name);
        }
        return singletonFactory.getIfLoaded(name);
    }

    public boolean hasPlugin(String name) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.hasPlugin(name);
        }
        return registry.supports(name);
    }

    public boolean hasPlugin(String name, String version) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.hasPlugin(name, version);
        }
        return registry.supports(name);
    }

    public Set<String> getSupportedPlugins() {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getSupportedPlugins();
        }
        return registry.getSupportedKeys();
    }

    public Set<T> getSupportedPluginInstances() {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getSupportedPluginInstances();
        }
        Set<T> instances = new LinkedHashSet<>();
        for (String name : getSupportedPlugins()) {
            instances.add(getOrCreatePlugin(name));
        }
        return Collections.unmodifiableSet(instances);
    }

    /**
     * Returns the keys already materialized in the local cache.
     */
    public Set<String> getLoadedPlugins() {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getLoadedPlugins();
        }
        return singletonFactory.loadedKeys();
    }

    /**
     * Test-only reset hook for one SPI type.
     */
    public static <T> void resetPluginDiscovery(Class<T> type) {
        DISCOVERIES.remove(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> PluginDiscovery<T> resolveSingleKeyDiscovery(
            Class<T> type,
            Function<T, String> keyExtractor,
            PluginDiscovery<?> existingRaw) {
        PluginDiscovery<T> existing = (PluginDiscovery<T>) existingRaw;
        PluginDiscovery<T> versioned = createBootstrapDiscovery(type, existing);
        if (versioned != null) {
            return versioned;
        }
        if (existing != null) {
            return existing;
        }

        try {
            ClasspathPluginLoader classpathLoader = new ClasspathPluginLoader();
            VersionedPluginRegistry<T> classpathRegistry = classpathLoader.load(type, keyExtractor);
            if (!classpathRegistry.isEmpty()) {
                log.info("Using versioned registry for {} (classpath/IDE mode)", type.getSimpleName());
                return new PluginDiscovery<>(type, VersionedPluginDiscovery.of(classpathRegistry));
            }
        } catch (Exception e) {
            log.debug("ClasspathPluginLoader failed for {}, falling back to legacy mode: {}",
                    type.getSimpleName(), e.getMessage());
        }

        KeyedRegistry<String, T> reg = KeyedRegistry.load(type, keyExtractor, type.getSimpleName());
        return new PluginDiscovery<>(type, reg);
    }

    @SuppressWarnings("unchecked")
    private static <T> PluginDiscovery<T> resolveMultiKeyDiscovery(
            Class<T> type,
            Function<T, Collection<String>> keysExtractor,
            PluginDiscovery<?> existingRaw) {
        PluginDiscovery<T> existing = (PluginDiscovery<T>) existingRaw;
        PluginDiscovery<T> versioned = createBootstrapDiscovery(type, existing);
        if (versioned != null) {
            return versioned;
        }
        if (existing != null) {
            return existing;
        }

        try {
            ClasspathPluginLoader classpathLoader = new ClasspathPluginLoader();
            VersionedPluginRegistry<T> classpathRegistry = classpathLoader.loadMultiKey(type, keysExtractor);
            if (!classpathRegistry.isEmpty()) {
                log.info("Using versioned registry for {} (classpath/IDE mode)", type.getSimpleName());
                return new PluginDiscovery<>(type, VersionedPluginDiscovery.of(classpathRegistry));
            }
        } catch (Exception e) {
            log.debug("ClasspathPluginLoader failed for {}, falling back to legacy mode: {}",
                    type.getSimpleName(), e.getMessage());
        }

        KeyedRegistry<String, T> reg = KeyedRegistry.loadMultiKey(type, keysExtractor, type.getSimpleName());
        return new PluginDiscovery<>(type, reg);
    }

    private static <T> PluginDiscovery<T> createBootstrapDiscovery(Class<T> type, PluginDiscovery<T> existing) {
        VersionedPluginRegistry<T> bootstrapRegistry = PluginDiscoveryBootstrap.getRegistry(type);
        if (bootstrapRegistry == null || bootstrapRegistry.isEmpty()) {
            return null;
        }
        if (existing != null
                && existing.versionedDiscovery != null
                && existing.versionedDiscovery.getRegistry() == bootstrapRegistry) {
            return existing;
        }
        log.info("Using versioned registry for {} (bootstrap/directory mode)", type.getSimpleName());
        return new PluginDiscovery<>(type, VersionedPluginDiscovery.of(bootstrapRegistry));
    }
}
