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
 * 多版本插件注册表。
 *
 * <p>数据结构：{@code Map<pluginName, NavigableMap<PluginVersion, P>>}
 *
 * <p>支持按名称+版本精确查找、按名称获取最新版本、按约束匹配等查询方式。
 * 注册表初始化后不可变，天然线程安全。
 *
 * <p>使用示例：
 * <pre>{@code
 * VersionedPluginRegistry<ConnectorFactory> registry =
 *     VersionedPluginRegistry.<ConnectorFactory>builder("ConnectorFactory")
 *         .register(descriptor, plugin, classLoader)
 *         .build();
 *
 * ConnectorFactory latest = registry.getLatest("mysql");
 * ConnectorFactory v8 = registry.get("mysql", "8.0.33");
 * }</pre>
 *
 * @param <P> 插件类型
 */
public final class VersionedPluginRegistry<P> {

    private final Map<String, NavigableMap<PluginVersion, P>> registry;
    private final Map<String, PluginClassLoader> classLoaders;
    private final String registryName;

    private VersionedPluginRegistry(
            Map<String, NavigableMap<PluginVersion, P>> registry,
            Map<String, PluginClassLoader> classLoaders,
            String registryName) {
        // 深度不可变包装
        Map<String, NavigableMap<PluginVersion, P>> immutable = new LinkedHashMap<String, NavigableMap<PluginVersion, P>>();
        for (Map.Entry<String, NavigableMap<PluginVersion, P>> entry : registry.entrySet()) {
            immutable.put(entry.getKey(),
                    Collections.unmodifiableNavigableMap(new TreeMap<PluginVersion, P>(entry.getValue())));
        }
        this.registry = Collections.unmodifiableMap(immutable);
        this.classLoaders = Collections.unmodifiableMap(new LinkedHashMap<String, PluginClassLoader>(classLoaders));
        this.registryName = registryName;
    }

    // ── 查询 ──────────────────────────────────────────────────

    /**
     * 获取最新版本（版本号最大）的插件实例。
     *
     * @param pluginName 插件名称
     * @return 最新版本的插件实例
     * @throws ProviderNotFoundException 如果插件不存在
     */
    public P getLatest(String pluginName) {
        NavigableMap<PluginVersion, P> versions = requireVersionMap(pluginName);
        return versions.lastEntry().getValue();
    }

    /**
     * 获取精确版本的插件实例。
     *
     * @param pluginName 插件名称
     * @param version    版本字符串
     * @return 指定版本的插件实例
     * @throws ProviderNotFoundException 如果版本不存在
     */
    public P get(String pluginName, String version) {
        return get(pluginName, PluginVersion.of(version));
    }

    /**
     * 获取精确版本的插件实例。
     *
     * @param pluginName 插件名称
     * @param version    版本对象
     * @return 指定版本的插件实例
     * @throws ProviderNotFoundException 如果版本不存在
     */
    public P get(String pluginName, PluginVersion version) {
        NavigableMap<PluginVersion, P> versions = requireVersionMap(pluginName);
        P plugin = versions.get(version);
        if (plugin == null) {
            throw new ProviderNotFoundException(registryName,
                    pluginName + "@" + version, versions.keySet());
        }
        return plugin;
    }

    /**
     * 按版本约束选择插件实例（优先最新满足条件的版本）。
     *
     * @param pluginName 插件名称
     * @param constraint 版本约束
     * @return 满足约束的最新版本插件实例
     * @throws ProviderNotFoundException 如果没有满足约束的版本
     */
    public P getCompatible(String pluginName, VersionConstraint constraint) {
        NavigableMap<PluginVersion, P> versions = requireVersionMap(pluginName);
        PluginVersion selected = constraint.selectLatest(versions.keySet());
        if (selected == null) {
            throw new ProviderNotFoundException(registryName,
                    pluginName + " matching " + constraint, versions.keySet());
        }
        return versions.get(selected);
    }

    /**
     * 获取某插件所有已注册版本及其实例（按版本升序）。
     */
    public NavigableMap<PluginVersion, P> getAllVersions(String pluginName) {
        return requireVersionMap(pluginName);
    }

    /**
     * 判断是否支持指定名称的插件（不区分版本）。
     */
    public boolean supportsPlugin(String pluginName) {
        return registry.containsKey(pluginName);
    }

    /**
     * 判断是否支持指定名称+版本的插件。
     */
    public boolean supportsVersion(String pluginName, String version) {
        NavigableMap<PluginVersion, P> versions = registry.get(pluginName);
        return versions != null && versions.containsKey(PluginVersion.of(version));
    }

    /**
     * 返回所有支持的插件名称。
     */
    public Set<String> supportedPluginNames() {
        return registry.keySet();
    }

    /**
     * 获取该版本插件的 ClassLoader。
     *
     * @param pluginId 插件唯一标识，如 "mysql@8.0.33"
     * @return 对应的 PluginClassLoader，可能为 null
     */
    public PluginClassLoader getClassLoader(String pluginId) {
        return classLoaders.get(pluginId);
    }

    /**
     * 返回注册表名称。
     */
    public String getRegistryName() {
        return registryName;
    }

    /**
     * 判断注册表是否为空。
     */
    public boolean isEmpty() {
        return registry.isEmpty();
    }

    // ── 构建 ──────────────────────────────────────────────────

    public static <P> Builder<P> builder(String registryName) {
        return new Builder<P>(registryName);
    }

    /**
     * 多版本注册表构建器。
     */
    public static final class Builder<P> {
        private final String registryName;
        private final Map<String, NavigableMap<PluginVersion, P>> map =
                new LinkedHashMap<String, NavigableMap<PluginVersion, P>>();
        private final Map<String, PluginClassLoader> classLoaders =
                new LinkedHashMap<String, PluginClassLoader>();

        Builder(String registryName) {
            this.registryName = registryName;
        }

        /**
         * 注册一个插件版本。
         *
         * <p>同一 pluginId（name@version）不允许重复注册。
         *
         * @param descriptor  插件描述符
         * @param plugin      插件实例
         * @param classLoader 插件的 ClassLoader（可为 null，表示来自系统 ClassLoader）
         * @return this
         * @throws DuplicateProviderException 同版本重复注册
         */
        public Builder<P> register(PluginDescriptor descriptor, P plugin,
                                   PluginClassLoader classLoader) {
            NavigableMap<PluginVersion, P> versions = map.get(descriptor.getPluginName());
            if (versions == null) {
                versions = new TreeMap<PluginVersion, P>();
                map.put(descriptor.getPluginName(), versions);
            }

            P existing = versions.get(descriptor.getVersion());
            if (existing != null) {
                throw new DuplicateProviderException(
                        registryName,
                        descriptor.getPluginId(),
                        existing.getClass().getName(),
                        plugin.getClass().getName());
            }
            versions.put(descriptor.getVersion(), plugin);

            if (classLoader != null) {
                PluginClassLoader previous = classLoaders.get(descriptor.getPluginId());
                if (previous != null) {
                    throw new IllegalStateException(
                            "Duplicate classloader for plugin " + descriptor.getPluginId());
                }
                classLoaders.put(descriptor.getPluginId(), classLoader);
            }

            return this;
        }

        /**
         * 简化注册（不需要 ClassLoader 时使用）。
         */
        public Builder<P> register(PluginDescriptor descriptor, P plugin) {
            return register(descriptor, plugin, null);
        }

        public VersionedPluginRegistry<P> build() {
            return new VersionedPluginRegistry<P>(map, classLoaders, registryName);
        }
    }

    // ── 内部 ──────────────────────────────────────────────────

    private NavigableMap<PluginVersion, P> requireVersionMap(String pluginName) {
        NavigableMap<PluginVersion, P> versions = registry.get(pluginName);
        if (versions == null || versions.isEmpty()) {
            throw new ProviderNotFoundException(registryName, pluginName, registry.keySet());
        }
        return versions;
    }
}
