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

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class VersionedPluginDiscoveryTest {

    @Test
    public void testGetOrCreatePlugin() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "5.7.44"), "mysql-5.7")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-8.0")
                        .build();

        VersionedPluginDiscovery<String> discovery = VersionedPluginDiscovery.of(registry);

        // 默认返回最新版本
        assertEquals("mysql-8.0", discovery.getOrCreatePlugin("mysql"));
    }

    @Test
    public void testGetOrCreatePluginWithVersion() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "5.7.44"), "mysql-5.7")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-8.0")
                        .build();

        VersionedPluginDiscovery<String> discovery = VersionedPluginDiscovery.of(registry);

        assertEquals("mysql-5.7", discovery.getOrCreatePlugin("mysql", "5.7.44"));
    }

    @Test
    public void testGetOrCreatePluginWithConstraint() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "5.7.44"), "mysql-5.7")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-8.0")
                        .build();

        VersionedPluginDiscovery<String> discovery = VersionedPluginDiscovery.of(registry);

        VersionConstraint constraint = VersionConstraint.parse("<8.0.0");
        assertEquals("mysql-5.7", discovery.getOrCreatePlugin("mysql", constraint));
    }

    @Test
    public void testHasPlugin() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .build();

        VersionedPluginDiscovery<String> discovery = VersionedPluginDiscovery.of(registry);

        assertTrue(discovery.hasPlugin("mysql"));
        assertFalse(discovery.hasPlugin("oracle"));
    }

    @Test
    public void testHasPluginWithVersion() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .build();

        VersionedPluginDiscovery<String> discovery = VersionedPluginDiscovery.of(registry);

        assertTrue(discovery.hasPlugin("mysql", "8.0.33"));
        assertFalse(discovery.hasPlugin("mysql", "5.7.0"));
    }

    @Test
    public void testGetSupportedPlugins() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .register(PluginDescriptor.of("postgresql", "42.7.0"), "pg-impl")
                        .build();

        VersionedPluginDiscovery<String> discovery = VersionedPluginDiscovery.of(registry);

        Set<String> supported = discovery.getSupportedPlugins();
        assertEquals(2, supported.size());
        assertTrue(supported.contains("mysql"));
        assertTrue(supported.contains("postgresql"));
    }

    @Test
    public void testCaching() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .build();

        VersionedPluginDiscovery<String> discovery = VersionedPluginDiscovery.of(registry);

        // 初始状态未加载
        assertNull(discovery.getLoadedPlugin("mysql"));
        assertTrue(discovery.getLoadedPlugins().isEmpty());

        // 调用后缓存
        String result = discovery.getOrCreatePlugin("mysql");
        assertEquals("mysql-impl", result);
        assertEquals("mysql-impl", discovery.getLoadedPlugin("mysql"));
        assertTrue(discovery.getLoadedPlugins().contains("mysql"));
    }
}
