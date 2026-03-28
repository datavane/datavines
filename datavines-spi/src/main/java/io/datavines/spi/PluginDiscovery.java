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

    private static final ConcurrentHashMap<Class<?>, PluginDiscovery<?>> DISCOVERIES = new ConcurrentHashMap<>();

    private final Class<T> type;
    private final KeyedRegistry<String, T> registry;
    private final CachingFactory<String, T> singletonFactory;

    private PluginDiscovery(Class<T> type, KeyedRegistry<String, T> registry) {
        this.type = type;
        this.registry = registry;
        this.singletonFactory = new CachingFactory<>(registry::get);
    }

    /**
     * 获取单名称插件的 PluginDiscovery 实例。
     * 每个插件类只注册一个名称。
     */
    @SuppressWarnings("unchecked")
    public static <T> PluginDiscovery<T> getPluginDiscovery(Class<T> type, Function<T, String> keyExtractor) {
        return (PluginDiscovery<T>) DISCOVERIES.computeIfAbsent(type,
            k -> {
                KeyedRegistry<String, T> reg = KeyedRegistry.load(type, keyExtractor, type.getSimpleName());
                return new PluginDiscovery<>(type, reg);
            });
    }

    /**
     * 获取多名称插件的 PluginDiscovery 实例。
     * 每个插件类可以注册多个名称（如 engine-prefixed names）。
     */
    @SuppressWarnings("unchecked")
    public static <T> PluginDiscovery<T> getMultiKeyPluginDiscovery(Class<T> type, Function<T, Collection<String>> keysExtractor) {
        return (PluginDiscovery<T>) DISCOVERIES.computeIfAbsent(type,
            k -> {
                KeyedRegistry<String, T> reg = KeyedRegistry.loadMultiKey(type, keysExtractor, type.getSimpleName());
                return new PluginDiscovery<>(type, reg);
            });
    }

    /**
     * 获取或创建缓存的插件实例（单例模式）。
     * 等价于旧 PluginLoader.getOrCreatePlugin(name)。
     */
    public T getOrCreatePlugin(String name) {
        return singletonFactory.get(name);
    }

    /**
     * 每次创建新实例。
     * 等价于旧 PluginLoader.getNewPlugin(name)。
     */
    public T getNewPlugin(String name) {
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
        return singletonFactory.getIfLoaded(name);
    }

    /**
     * 判断是否支持指定名称的插件。
     * 等价于旧 PluginLoader.hasPlugin(name)。
     */
    public boolean hasPlugin(String name) {
        return registry.supports(name);
    }

    /**
     * 返回所有支持的插件名称。
     * 等价于旧 PluginLoader.getSupportedPlugins()。
     */
    public Set<String> getSupportedPlugins() {
        return registry.getSupportedKeys();
    }

    /**
     * 获取所有支持的插件实例（按需创建并缓存）。
     * 等价于旧 PluginLoader.getSupportedPluginInstances()。
     */
    public Set<T> getSupportedPluginInstances() {
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
        return singletonFactory.loadedKeys();
    }

    /**
     * 重置指定类型的 Discovery（主要用于测试）。
     */
    public static <T> void resetPluginDiscovery(Class<T> type) {
        DISCOVERIES.remove(type);
    }
}
