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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import io.datavines.spi.PluginDescriptor;

/**
 * Utility methods around {@link ServiceLoader}.
 */
public final class ServiceLoaderUtils {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoaderUtils.class);

    private ServiceLoaderUtils() {}

    /**
     * Loads all providers and skips entries that fail to initialize.
     */
    public static <S> List<S> loadAll(Class<S> serviceType) {
        return loadAll(serviceType, Thread.currentThread().getContextClassLoader());
    }

    public static <S> List<S> loadAll(Class<S> serviceType, ClassLoader classLoader) {
        List<S> result = new ArrayList<>();
        ServiceLoader<S> loader = ServiceLoader.load(serviceType, classLoader);
        Iterator<S> iterator = loader.iterator();

        while (iterator.hasNext()) {
            try {
                result.add(iterator.next());
            } catch (ServiceConfigurationError e) {
                log.error("Failed to load provider for {}: {}",
                    serviceType.getName(), e.getMessage(), e);
            }
        }

        log.debug("Loaded {} providers for {}", result.size(), serviceType.getName());
        return result;
    }

    public static <S> List<S> loadAllStrict(Class<S> serviceType) {
        List<S> result = new ArrayList<>();
        for (S provider : ServiceLoader.load(serviceType)) {
            result.add(provider);
        }
        return result;
    }

    public static <S> List<PluginEntry<S>> loadWithDescriptor(
            Class<S> serviceType, ClassLoader classLoader) {

        List<PluginEntry<S>> result = new ArrayList<>();
        PluginDescriptor descriptor = PluginDescriptor.load(classLoader);

        ServiceLoader<S> loader = ServiceLoader.load(serviceType, classLoader);
        Iterator<S> it = loader.iterator();

        while (it.hasNext()) {
            try {
                S instance = it.next();
                result.add(new PluginEntry<S>(descriptor, instance));
            } catch (ServiceConfigurationError e) {
                log.error("Failed to load {} provider: {}",
                        serviceType.getName(), e.getMessage(), e);
            }
        }
        return result;
    }

    public static final class PluginEntry<S> {
        private final PluginDescriptor descriptor; // nullable
        private final S instance;

        public PluginEntry(PluginDescriptor descriptor, S instance) {
            this.descriptor = descriptor;
            this.instance = instance;
        }

        public PluginDescriptor getDescriptor() {
            return descriptor;
        }

        public S getInstance() {
            return instance;
        }

        public boolean hasDescriptor() {
            return descriptor != null;
        }
    }
}
