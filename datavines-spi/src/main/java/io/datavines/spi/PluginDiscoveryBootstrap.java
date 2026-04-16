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
 * Global holder for registries built during bootstrap.
 */
public final class PluginDiscoveryBootstrap {

    private static final Logger log = LoggerFactory.getLogger(PluginDiscoveryBootstrap.class);

    private static volatile Map<Class<?>, VersionedPluginRegistry<?>> globalRegistries;

    private PluginDiscoveryBootstrap() {}

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

    @SuppressWarnings("unchecked")
    public static <T> VersionedPluginRegistry<T> getRegistry(Class<T> type) {
        Map<Class<?>, VersionedPluginRegistry<?>> regs = globalRegistries;
        if (regs == null) {
            return null;
        }
        return (VersionedPluginRegistry<T>) regs.get(type);
    }

    public static boolean isInitialized() {
        return globalRegistries != null;
    }

    /**
     * Test-only reset hook.
     */
    public static synchronized void reset() {
        globalRegistries = null;
        log.debug("PluginDiscoveryBootstrap reset");
    }
}
