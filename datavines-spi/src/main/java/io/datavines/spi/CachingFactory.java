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

public final class CachingFactory<K, T> {

    private final ConcurrentHashMap<K, T> cache = new ConcurrentHashMap<>();
    private final Function<K, T> creator;

    public CachingFactory(Function<K, T> creator) {
        this.creator = creator;
    }

    public T get(K key) {
        return cache.computeIfAbsent(key, creator);
    }

    public T create(K key) {
        return creator.apply(key);
    }

    public Set<K> loadedKeys() {
        return Collections.unmodifiableSet(new TreeSet<>(cache.keySet()));
    }

    public List<T> loadedInstances() {
        return Collections.unmodifiableList(new ArrayList<>(cache.values()));
    }

    public T getIfLoaded(K key) {
        return cache.get(key);
    }

    public boolean isLoaded(K key) {
        return cache.containsKey(key);
    }

    public void clear() {
        cache.clear();
    }

    public void evict(K key) {
        cache.remove(key);
    }

    public int cacheSize() {
        return cache.size();
    }
}
