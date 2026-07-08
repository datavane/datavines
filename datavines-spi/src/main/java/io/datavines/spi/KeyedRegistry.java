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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 唯一型注册表：一个 Key 有且仅有一个 Provider。
 *
 * <p>初始化完成后不可变，天然线程安全。
 *
 * @param <K> Key 类型
 * @param <P> Provider 类型
 */
public final class KeyedRegistry<K, P> {

    private static final Logger log = LoggerFactory.getLogger(KeyedRegistry.class);

    private final Map<K, P> providers;
    private final String registryName;

    private KeyedRegistry(Map<K, P> providers, String registryName) {
        this.providers = Collections.unmodifiableMap(providers);
        this.registryName = registryName;
    }

    /**
     * 从 ServiceLoader 加载所有 Provider 并构建注册表。
     *
     * @param providerType   Provider 的 SPI 接口类型
     * @param keyExtractor   从 Provider 中提取 Key 的函数
     * @return 不可变的注册表
     * @throws DuplicateProviderException 同一个 Key 被多个 Provider 声明
     */
    public static <K, P> KeyedRegistry<K, P> load(
            Class<P> providerType,
            Function<P, K> keyExtractor) {
        return load(providerType, keyExtractor, providerType.getSimpleName());
    }

    /**
     * 从 ServiceLoader 加载，指定注册表名称（用于日志和异常信息）。
     */
    public static <K, P> KeyedRegistry<K, P> load(
            Class<P> providerType,
            Function<P, K> keyExtractor,
            String registryName) {
        return from(ServiceLoaderUtils.loadAll(providerType), keyExtractor, registryName);
    }

    /**
     * 从显式 Provider 列表构建注册表（测试和手动组装场景）。
     */
    @SafeVarargs
    public static <K, P> KeyedRegistry<K, P> of(
            Function<P, K> keyExtractor,
            P... providers) {
        return from(Arrays.asList(providers), keyExtractor, "test");
    }

    /**
     * 从可迭代的 Provider 集合构建注册表。
     *
     * @param providers      Provider 集合
     * @param keyExtractor   Key 提取函数
     * @param registryName   注册表名称（用于日志和异常信息）
     * @return 不可变的注册表
     * @throws DuplicateProviderException 同一个 Key 被多个 Provider 声明
     */
    public static <K, P> KeyedRegistry<K, P> from(
            Iterable<P> providers,
            Function<P, K> keyExtractor,
            String registryName) {

        Map<K, P> map = new LinkedHashMap<>();

        for (P provider : providers) {
            K key = keyExtractor.apply(provider);
            Objects.requireNonNull(key,
                () -> "Provider " + provider.getClass().getName()
                    + " returned null key in registry [" + registryName + "]");

            P existing = map.put(key, provider);
            if (existing != null) {
                throw new DuplicateProviderException(
                    registryName, key,
                    existing.getClass().getName(),
                    provider.getClass().getName());
            }

            log.info("[{}] Registered: {} -> {}",
                registryName, key, provider.getClass().getName());
        }

        log.info("[{}] Initialized with {} providers: {}",
            registryName, map.size(), map.keySet());

        return new KeyedRegistry<>(map, registryName);
    }

    /**
     * 从 ServiceLoader 加载，每个 Provider 可以注册多个 Key。
     * 适用于同一个类注册多个 engine-prefixed 名称的场景。
     */
    public static <K, P> KeyedRegistry<K, P> loadMultiKey(
            Class<P> providerType,
            Function<P, Collection<K>> keysExtractor,
            String registryName) {
        return fromMultiKey(ServiceLoaderUtils.loadAll(providerType), keysExtractor, registryName);
    }

    /**
     * 从 Provider 集合构建注册表，每个 Provider 注册多个 Key。
     */
    public static <K, P> KeyedRegistry<K, P> fromMultiKey(
            Iterable<P> providers,
            Function<P, Collection<K>> keysExtractor,
            String registryName) {

        Map<K, P> map = new LinkedHashMap<>();

        for (P provider : providers) {
            Collection<K> keys = keysExtractor.apply(provider);
            if (keys == null || keys.isEmpty()) {
                log.warn("[{}] Provider {} returned empty keys, skipping",
                    registryName, provider.getClass().getName());
                continue;
            }

            for (K key : keys) {
                Objects.requireNonNull(key,
                    () -> "Provider " + provider.getClass().getName()
                        + " returned null key in registry [" + registryName + "]");

                P existing = map.put(key, provider);
                if (existing != null) {
                    throw new DuplicateProviderException(
                        registryName, key,
                        existing.getClass().getName(),
                        provider.getClass().getName());
                }

                log.info("[{}] Registered: {} -> {}",
                    registryName, key, provider.getClass().getName());
            }
        }

        log.info("[{}] Initialized with {} keys from providers",
            registryName, map.size());

        return new KeyedRegistry<>(map, registryName);
    }

    /**
     * 获取指定 Key 对应的 Provider。
     *
     * @throws ProviderNotFoundException 如果 Key 不存在
     */
    public P get(K key) {
        P provider = providers.get(key);
        if (provider == null) {
            throw new ProviderNotFoundException(registryName, key, providers.keySet());
        }
        return provider;
    }

    /**
     * 查找指定 Key 对应的 Provider，不存在时返回 null。
     */
    public P find(K key) {
        return providers.get(key);
    }

    public boolean supports(K key) {
        return providers.containsKey(key);
    }

    public Set<K> keys() {
        return providers.keySet();
    }

    /**
     * 返回所有已支持的 Key。
     */
    public Set<K> getSupportedKeys() {
        return keys();
    }

    /**
     * 返回所有已注册 Provider。
     */
    public Collection<P> providers() {
        return providers.values();
    }

    /**
     * 返回 Key 到 Provider 的只读映射。
     */
    public Map<K, P> asMap() {
        return providers;
    }

    public int size() {
        return providers.size();
    }
}
