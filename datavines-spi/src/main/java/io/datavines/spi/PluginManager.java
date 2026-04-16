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
 * Convenience wrapper around {@link KeyedRegistry} plus {@link CachingFactory}.
 */
public final class PluginManager<K, P> {

    private final KeyedRegistry<K, P> registry;
    private final CachingFactory<K, P> factory;

    private PluginManager(KeyedRegistry<K, P> registry, CachingFactory<K, P> factory) {
        this.registry = registry;
        this.factory = factory;
    }

    public static <K, P> PluginManager<K, P> load(
            Class<P> providerType,
            Function<P, K> keyExtractor,
            Function<P, P> instanceCreator,
            String managerName) {

        KeyedRegistry<K, P> registry = KeyedRegistry.load(providerType, keyExtractor, managerName);
        CachingFactory<K, P> factory = new CachingFactory<>(key -> instanceCreator.apply(registry.get(key)));
        return new PluginManager<>(registry, factory);
    }

    public static <K, P> PluginManager<K, P> loadSelfManaged(
            Class<P> providerType,
            Function<P, K> keyExtractor,
            String managerName) {

        KeyedRegistry<K, P> registry = KeyedRegistry.load(providerType, keyExtractor, managerName);
        CachingFactory<K, P> factory = new CachingFactory<>(registry::get);
        return new PluginManager<>(registry, factory);
    }

    public P get(K key) {
        return factory.get(key);
    }

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
