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
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 唯一型扩展的统一入口。
 *
 * <p>内部组合 {@link KeyedRegistry} 和 {@link CachingFactory}，
 * 适用于"一个 Key 对应一个 Provider"的场景。
 *
 * @param <K> Key 类型
 * @param <P> Provider 类型（在 datavines 中通常 P 就是最终实例类型）
 */
public final class PluginManager<K, P> {

    private final KeyedRegistry<K, P> registry;
    private final CachingFactory<K, P> factory;

    private PluginManager(KeyedRegistry<K, P> registry, CachingFactory<K, P> factory) {
        this.registry = registry;
        this.factory = factory;
    }

    /**
     * 从 ServiceLoader 加载并构建 PluginManager。
     *
     * @param providerType   SPI 接口类型
     * @param keyExtractor   从实例中提取 Key 的函数
     * @param instanceCreator 创建新实例的函数（从注册表中的 Provider 创建）
     * @param managerName    管理器名称（用于日志和异常）
     */
    public static <K, P> PluginManager<K, P> load(
            Class<P> providerType,
            Function<P, K> keyExtractor,
            Function<P, P> instanceCreator,
            String managerName) {

        KeyedRegistry<K, P> registry = KeyedRegistry.load(providerType, keyExtractor, managerName);
        CachingFactory<K, P> factory = new CachingFactory<>(key -> instanceCreator.apply(registry.get(key)));
        return new PluginManager<>(registry, factory);
    }

    /**
     * 简化版本：Provider 即 Instance（T = P），缓存 Provider 自身。
     */
    public static <K, P> PluginManager<K, P> loadSelfManaged(
            Class<P> providerType,
            Function<P, K> keyExtractor,
            String managerName) {

        KeyedRegistry<K, P> registry = KeyedRegistry.load(providerType, keyExtractor, managerName);
        // 缓存模式：直接返回注册表中的 Provider 实例
        CachingFactory<K, P> factory = new CachingFactory<>(registry::get);
        return new PluginManager<>(registry, factory);
    }

    /**
     * 获取缓存实例（单例模式）。
     */
    public P get(K key) {
        return factory.get(key);
    }

    /**
     * 创建新实例，不走缓存。
     * 通过反射创建新实例（用于需要每次新建的场景）。
     */
    public P createNew(K key) {
        P provider = registry.get(key);
        try {
            @SuppressWarnings("unchecked")
            P newInstance = (P) provider.getClass().newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to create new instance of " + provider.getClass().getName(), e);
        }
    }

    public P getIfLoaded(K key) {
        return factory.getIfLoaded(key);
    }

    public boolean supports(K key) {
        return registry.supports(key);
    }

    public P getProvider(K key) {
        return registry.get(key);
    }

    public P findProvider(K key) {
        return registry.find(key);
    }

    public Set<K> getSupportedKeys() {
        return registry.getSupportedKeys();
    }

    public Collection<P> getSupportedProviders() {
        return registry.providers();
    }

    public Set<K> getLoadedKeys() {
        return factory.loadedKeys();
    }

    public List<P> getLoadedInstances() {
        return factory.loadedInstances();
    }

    public void clearCache() {
        factory.clear();
    }

    public void evict(K key) {
        factory.evict(key);
    }

    public KeyedRegistry<K, P> getRegistry() {
        return registry;
    }
}
