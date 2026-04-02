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
 * 基于 {@link VersionedPluginRegistry} 的向后兼容门面。
 *
 * <p>保留与现有 {@link PluginDiscovery} 相同的调用方式：
 * {@code getOrCreatePlugin(name)} 默认返回最新版本。
 * 同时扩展了按版本和按约束获取插件的能力。
 *
 * <p>使用示例：
 * <pre>{@code
 * VersionedPluginDiscovery<ConnectorFactory> discovery =
 *     VersionedPluginDiscovery.of(registry);
 *
 * // 兼容旧调用方式
 * ConnectorFactory factory = discovery.getOrCreatePlugin("mysql");
 *
 * // 新增：指定版本
 * ConnectorFactory v8 = discovery.getOrCreatePlugin("mysql", "8.0.33");
 *
 * // 新增：按约束
 * ConnectorFactory compat = discovery.getOrCreatePlugin("mysql",
 *     VersionConstraint.parse(">=8.0.0 <9.0.0"));
 * }</pre>
 *
 * @param <T> 插件接口类型
 */
public final class VersionedPluginDiscovery<T> {

    private final VersionedPluginRegistry<T> registry;
    private final CachingFactory<String, T> latestCache;

    private VersionedPluginDiscovery(VersionedPluginRegistry<T> registry) {
        this.registry = registry;
        this.latestCache = new CachingFactory<String, T>(new LatestVersionCreator<T>(registry));
    }

    /**
     * 从 VersionedPluginRegistry 创建。
     */
    public static <T> VersionedPluginDiscovery<T> of(VersionedPluginRegistry<T> registry) {
        return new VersionedPluginDiscovery<T>(registry);
    }

    // ── 与现有 PluginDiscovery 等价的方法 ────────────────────

    /**
     * 默认：返回最新版本（与旧 getOrCreatePlugin 等价）。
     * 首次调用时创建并缓存。
     */
    public T getOrCreatePlugin(String name) {
        return latestCache.get(name);
    }

    /**
     * 新增：明确指定版本。
     */
    public T getOrCreatePlugin(String name, String version) {
        return registry.get(name, version);
    }

    /**
     * 新增：按约束选版本。
     */
    public T getOrCreatePlugin(String name, VersionConstraint constraint) {
        return registry.getCompatible(name, constraint);
    }

    /**
     * 返回已缓存实例，不存在则返回 null。
     */
    public T getLoadedPlugin(String name) {
        return latestCache.getIfLoaded(name);
    }

    /**
     * 返回最新版本的新实例（通过反射创建）。
     */
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

    /**
     * 获取所有支持的插件实例（每个插件取最新版本，按需创建并缓存）。
     */
    public Set<T> getSupportedPluginInstances() {
        Set<T> instances = new LinkedHashSet<T>();
        for (String name : getSupportedPlugins()) {
            instances.add(getOrCreatePlugin(name));
        }
        return Collections.unmodifiableSet(instances);
    }

    /**
     * 获取底层 registry（高级用途）。
     */
    public VersionedPluginRegistry<T> getRegistry() {
        return registry;
    }

    /**
     * 用于 CachingFactory 的函数对象，将 name 映射到最新版本实例。
     * 使用独立类而非 lambda 以保证 Java 8 兼容性和可调试性。
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
