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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 多版本插件启动引导器。
 *
 * <p>在服务启动期调用 {@link #initialize(Map)} 注入 {@link VersionedPluginRegistry}，
 * 之后 {@link PluginDiscovery} 会优先使用已注册的 registry 来获取插件实例。
 *
 * <p>使用示例：
 * <pre>{@code
 * // 服务启动时
 * PluginDirectoryLoader loader = new PluginDirectoryLoader(...);
 * VersionedPluginRegistry<ConnectorFactory> connectorRegistry =
 *     loader.load(ConnectorFactory.class);
 *
 * Map<Class<?>, VersionedPluginRegistry<?>> registries = new HashMap<>();
 * registries.put(ConnectorFactory.class, connectorRegistry);
 * PluginDiscoveryBootstrap.initialize(registries);
 *
 * // 之后 PluginDiscovery.getOrCreatePlugin("mysql") 将从 registry 获取最新版本
 * }</pre>
 *
 * <p>线程安全：{@link #initialize(Map)} 应在服务启动期单次调用，之后只读。
 * 使用 volatile 确保 happens-before 语义。
 */
public final class PluginDiscoveryBootstrap {

    private static final Logger log = LoggerFactory.getLogger(PluginDiscoveryBootstrap.class);

    private static volatile Map<Class<?>, VersionedPluginRegistry<?>> globalRegistries;

    private PluginDiscoveryBootstrap() {}

    /**
     * 初始化全局版本化注册表。
     *
     * <p>应在服务启动期调用一次。重复调用将覆盖之前的注册表并记录警告。
     *
     * @param registries 类型 → 版本化注册表 的映射
     */
    public static synchronized void initialize(Map<Class<?>, VersionedPluginRegistry<?>> registries) {
        if (globalRegistries != null) {
            log.warn("PluginDiscoveryBootstrap is being re-initialized. "
                    + "Previous registries will be replaced.");
        }
        globalRegistries = Collections.unmodifiableMap(
                new HashMap<Class<?>, VersionedPluginRegistry<?>>(registries));
        log.info("PluginDiscoveryBootstrap initialized with {} registry types: {}",
                registries.size(), registries.keySet());
    }

    /**
     * 获取指定类型的版本化注册表。
     *
     * @param type SPI 接口类型
     * @return 对应的 VersionedPluginRegistry，未注册则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> VersionedPluginRegistry<T> getRegistry(Class<T> type) {
        Map<Class<?>, VersionedPluginRegistry<?>> regs = globalRegistries;
        if (regs == null) {
            return null;
        }
        return (VersionedPluginRegistry<T>) regs.get(type);
    }

    /**
     * 检查是否已初始化。
     */
    public static boolean isInitialized() {
        return globalRegistries != null;
    }

    /**
     * 重置（仅用于测试）。
     */
    public static synchronized void reset() {
        globalRegistries = null;
        log.debug("PluginDiscoveryBootstrap reset");
    }
}
