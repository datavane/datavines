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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PluginLoader 的替代品，提供与旧 API 兼容的接口。
 *
 * <p>基于 JDK {@link java.util.ServiceLoader} + {@link KeyedRegistry} + {@link CachingFactory} 实现。
 * 支持单名称插件和多名称插件（同一个类注册多个名称）。
 *
 * <p>使用示例：
 * <pre>{@code
 * // 单名称插件
 * PluginDiscovery<ConnectorFactory> discovery =
 *     PluginDiscovery.getPluginDiscovery(ConnectorFactory.class, ConnectorFactory::getPluginName);
 *
 * // 多名称插件
 * PluginDiscovery<ExpectedValue> discovery =
 *     PluginDiscovery.getMultiKeyPluginDiscovery(ExpectedValue.class, ExpectedValue::getPluginNames);
 *
 * // 获取或创建缓存的插件实例
 * ConnectorFactory factory = discovery.getOrCreatePlugin("mysql");
 * }</pre>
 *
 * @param <T> 插件接口类型
 */
public final class PluginDiscovery<T> {

    private static final Logger log = LoggerFactory.getLogger(PluginDiscovery.class);

    private static final ConcurrentHashMap<Class<?>, PluginDiscovery<?>> DISCOVERIES = new ConcurrentHashMap<>();

    private final Class<T> type;
    private final KeyedRegistry<String, T> registry;
    private final CachingFactory<String, T> singletonFactory;
    // 多版本支持：如果 bootstrap 中注册了该类型的 VersionedPluginRegistry，优先使用
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
     * 获取单名称插件的 PluginDiscovery 实例。
     * 每个插件类只注册一个名称。
     *
     * <p>加载顺序：
     * <ol>
     *   <li>{@link PluginDiscoveryBootstrap}（生产目录模式）</li>
     *   <li>{@link ClasspathPluginLoader}（IDE/classpath 模式，自动读取 datavines-plugin.properties）</li>
     *   <li>{@link KeyedRegistry} + {@link java.util.ServiceLoader}（传统无版本模式）</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    public static <T> PluginDiscovery<T> getPluginDiscovery(Class<T> type, Function<T, String> keyExtractor) {
        return (PluginDiscovery<T>) DISCOVERIES.computeIfAbsent(type,
            k -> {
                // 1. 优先从 bootstrap 获取（生产目录模式）
                VersionedPluginRegistry<T> bootstrapRegistry = PluginDiscoveryBootstrap.getRegistry(type);
                if (bootstrapRegistry != null && !bootstrapRegistry.isEmpty()) {
                    log.info("Using versioned registry for {} (bootstrap/directory mode)", type.getSimpleName());
                    VersionedPluginDiscovery<T> vd = VersionedPluginDiscovery.of(bootstrapRegistry);
                    return new PluginDiscovery<>(type, vd);
                }
                // 2. 尝试 classpath 模式（IDE/开发模式）
                try {
                    ClasspathPluginLoader classpathLoader = new ClasspathPluginLoader();
                    VersionedPluginRegistry<T> classpathRegistry = classpathLoader.load(type, keyExtractor);
                    if (!classpathRegistry.isEmpty()) {
                        log.info("Using versioned registry for {} (classpath/IDE mode)", type.getSimpleName());
                        VersionedPluginDiscovery<T> vd = VersionedPluginDiscovery.of(classpathRegistry);
                        return new PluginDiscovery<>(type, vd);
                    }
                } catch (Exception e) {
                    log.debug("ClasspathPluginLoader failed for {}, falling back to legacy mode: {}",
                            type.getSimpleName(), e.getMessage());
                }
                // 3. 回退到传统 ServiceLoader 方式（无版本）
                KeyedRegistry<String, T> reg = KeyedRegistry.load(type, keyExtractor, type.getSimpleName());
                return new PluginDiscovery<>(type, reg);
            });
    }

    /**
     * 获取多名称插件的 PluginDiscovery 实例。
     * 每个插件类可以注册多个名称（如 engine-prefixed names）。
     *
     * <p>加载顺序同 {@link #getPluginDiscovery(Class, Function)}。
     */
    @SuppressWarnings("unchecked")
    public static <T> PluginDiscovery<T> getMultiKeyPluginDiscovery(Class<T> type, Function<T, Collection<String>> keysExtractor) {
        return (PluginDiscovery<T>) DISCOVERIES.computeIfAbsent(type,
            k -> {
                // 1. 优先从 bootstrap 获取（生产目录模式）
                VersionedPluginRegistry<T> bootstrapRegistry = PluginDiscoveryBootstrap.getRegistry(type);
                if (bootstrapRegistry != null && !bootstrapRegistry.isEmpty()) {
                    log.info("Using versioned registry for {} (bootstrap/directory mode)", type.getSimpleName());
                    VersionedPluginDiscovery<T> vd = VersionedPluginDiscovery.of(bootstrapRegistry);
                    return new PluginDiscovery<>(type, vd);
                }
                // 2. 尝试 classpath 模式（IDE/开发模式）
                try {
                    ClasspathPluginLoader classpathLoader = new ClasspathPluginLoader();
                    VersionedPluginRegistry<T> classpathRegistry = classpathLoader.loadMultiKey(type, keysExtractor);
                    if (!classpathRegistry.isEmpty()) {
                        log.info("Using versioned registry for {} (classpath/IDE mode)", type.getSimpleName());
                        VersionedPluginDiscovery<T> vd = VersionedPluginDiscovery.of(classpathRegistry);
                        return new PluginDiscovery<>(type, vd);
                    }
                } catch (Exception e) {
                    log.debug("ClasspathPluginLoader failed for {}, falling back to legacy mode: {}",
                            type.getSimpleName(), e.getMessage());
                }
                // 3. 回退到传统 ServiceLoader 方式（无版本）
                KeyedRegistry<String, T> reg = KeyedRegistry.loadMultiKey(type, keysExtractor, type.getSimpleName());
                return new PluginDiscovery<>(type, reg);
            });
    }

    /**
     * 获取或创建缓存的插件实例（单例模式）。
     * 等价于旧 PluginLoader.getOrCreatePlugin(name)。
     *
     * <p>当存在多版本注册表时，默认返回最新版本。
     */
    public T getOrCreatePlugin(String name) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getOrCreatePlugin(name);
        }
        return singletonFactory.get(name);
    }

    /**
     * 新增：获取指定版本的插件实例。
     * 仅在多版本模式下有效，否则忽略版本参数。
     */
    public T getOrCreatePlugin(String name, String version) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getOrCreatePlugin(name, version);
        }
        return singletonFactory.get(name);
    }

    /**
     * 新增：按版本约束获取插件实例。
     * 仅在多版本模式下有效，否则忽略约束参数。
     */
    public T getOrCreatePlugin(String name, VersionConstraint constraint) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getOrCreatePlugin(name, constraint);
        }
        return singletonFactory.get(name);
    }

    /**
     * 每次创建新实例。
     * 等价于旧 PluginLoader.getNewPlugin(name)。
     */
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

    /**
     * 获取已加载的插件实例，未加载则返回 null。
     * 等价于旧 PluginLoader.getLoadedPlugin(name)。
     */
    public T getLoadedPlugin(String name) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getLoadedPlugin(name);
        }
        return singletonFactory.getIfLoaded(name);
    }

    /**
     * 判断是否支持指定名称的插件。
     * 等价于旧 PluginLoader.hasPlugin(name)。
     */
    public boolean hasPlugin(String name) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.hasPlugin(name);
        }
        return registry.supports(name);
    }

    /**
     * 判断是否支持指定名称+版本的插件。
     */
    public boolean hasPlugin(String name, String version) {
        if (versionedDiscovery != null) {
            return versionedDiscovery.hasPlugin(name, version);
        }
        // 非多版本模式下忽略版本参数
        return registry.supports(name);
    }

    /**
     * 返回所有支持的插件名称。
     * 等价于旧 PluginLoader.getSupportedPlugins()。
     */
    public Set<String> getSupportedPlugins() {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getSupportedPlugins();
        }
        return registry.getSupportedKeys();
    }

    /**
     * 获取所有支持的插件实例（按需创建并缓存）。
     * 等价于旧 PluginLoader.getSupportedPluginInstances()。
     */
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
     * 返回已加载的插件名称集合。
     * 等价于旧 PluginLoader.getLoadedPlugins()。
     */
    public Set<String> getLoadedPlugins() {
        if (versionedDiscovery != null) {
            return versionedDiscovery.getLoadedPlugins();
        }
        return singletonFactory.loadedKeys();
    }

    /**
     * 获取多版本注册表的门面（如果可用）。
     *
     * @return VersionedPluginDiscovery 或 null
     */
    public VersionedPluginDiscovery<T> getVersionedDiscovery() {
        return versionedDiscovery;
    }

    /**
     * 重置指定类型的 Discovery（主要用于测试）。
     */
    public static <T> void resetPluginDiscovery(Class<T> type) {
        DISCOVERIES.remove(type);
    }
}
