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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 线程安全的带缓存实例工厂。
 *
 * <p>适用于"无配置实例全局缓存、有配置实例每次新建"的场景。
 *
 * @param <K> Key 类型
 * @param <T> 实例类型
 */
public final class CachingFactory<K, T> {

    private final ConcurrentHashMap<K, T> cache = new ConcurrentHashMap<>();
    private final Function<K, T> creator;

    public CachingFactory(Function<K, T> creator) {
        this.creator = creator;
    }

    /**
     * 获取缓存实例。同一个 Key 始终返回同一个实例。
     */
    public T get(K key) {
        return cache.computeIfAbsent(key, creator);
    }

    /**
     * 创建新实例，不走缓存。
     */
    public T create(K key) {
        return creator.apply(key);
    }

    /**
     * 返回已加载（已创建并进入缓存）的 Key 集合。
     */
    public Set<K> loadedKeys() {
        return Collections.unmodifiableSet(new TreeSet<>(cache.keySet()));
    }

    /**
     * 返回已加载实例，不触发新的创建。
     */
    public List<T> loadedInstances() {
        return Collections.unmodifiableList(new ArrayList<>(cache.values()));
    }

    /**
     * 返回指定 Key 的已加载实例；如果尚未创建则返回 null，不触发加载。
     */
    public T getIfLoaded(K key) {
        return cache.get(key);
    }

    /**
     * 判断指定 Key 是否已经创建并进入缓存。
     */
    public boolean isLoaded(K key) {
        return cache.containsKey(key);
    }

    /**
     * 清除所有缓存。
     */
    public void clear() {
        cache.clear();
    }

    /**
     * 清除指定 Key 的缓存。
     */
    public void evict(K key) {
        cache.remove(key);
    }

    public int cacheSize() {
        return cache.size();
    }
}
