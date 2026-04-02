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

import java.util.NavigableMap;
import java.util.Set;

import static org.junit.Assert.*;

public class VersionedPluginRegistryTest {

    @Test
    public void testRegisterAndGetLatest() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "5.7.44"), "mysql-5.7")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-8.0")
                        .build();

        assertEquals("mysql-8.0", registry.getLatest("mysql"));
    }

    @Test
    public void testGetExactVersion() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "5.7.44"), "mysql-5.7")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-8.0")
                        .build();

        assertEquals("mysql-5.7", registry.get("mysql", "5.7.44"));
        assertEquals("mysql-8.0", registry.get("mysql", "8.0.33"));
    }

    @Test
    public void testGetCompatible() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "5.7.44"), "mysql-5.7")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-8.0")
                        .register(PluginDescriptor.of("mysql", "8.0.35"), "mysql-8.0.35")
                        .build();

        VersionConstraint constraint = VersionConstraint.parse(">=8.0.0 <9.0.0");
        assertEquals("mysql-8.0.35", registry.getCompatible("mysql", constraint));
    }

    @Test
    public void testGetAllVersions() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "5.7.44"), "mysql-5.7")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-8.0")
                        .build();

        NavigableMap<PluginVersion, String> versions = registry.getAllVersions("mysql");
        assertEquals(2, versions.size());
        assertEquals(PluginVersion.of("5.7.44"), versions.firstKey());
        assertEquals(PluginVersion.of("8.0.33"), versions.lastKey());
    }

    @Test
    public void testMultiplePlugins() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .register(PluginDescriptor.of("postgresql", "42.7.0"), "pg-impl")
                        .build();

        Set<String> names = registry.supportedPluginNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("mysql"));
        assertTrue(names.contains("postgresql"));
    }

    @Test
    public void testSupportsPlugin() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .build();

        assertTrue(registry.supportsPlugin("mysql"));
        assertFalse(registry.supportsPlugin("oracle"));
    }

    @Test
    public void testSupportsVersion() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .build();

        assertTrue(registry.supportsVersion("mysql", "8.0.33"));
        assertFalse(registry.supportsVersion("mysql", "5.7.0"));
    }

    @Test(expected = ProviderNotFoundException.class)
    public void testGetNonexistentPlugin() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry").build();

        registry.getLatest("nonexistent");
    }

    @Test(expected = ProviderNotFoundException.class)
    public void testGetNonexistentVersion() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .build();

        registry.get("mysql", "5.7.0");
    }

    @Test(expected = DuplicateProviderException.class)
    public void testDuplicateVersion() {
        VersionedPluginRegistry.<String>builder("TestRegistry")
                .register(PluginDescriptor.of("mysql", "8.0.33"), "impl-1")
                .register(PluginDescriptor.of("mysql", "8.0.33"), "impl-2")
                .build();
    }

    @Test
    public void testIsEmpty() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry").build();
        assertTrue(registry.isEmpty());
    }

    @Test
    public void testNotEmpty() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .build();
        assertFalse(registry.isEmpty());
    }

    @Test
    public void testImmutability() {
        VersionedPluginRegistry<String> registry =
                VersionedPluginRegistry.<String>builder("TestRegistry")
                        .register(PluginDescriptor.of("mysql", "8.0.33"), "mysql-impl")
                        .build();

        try {
            registry.getAllVersions("mysql").put(PluginVersion.of("9.0.0"), "new-impl");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            registry.supportedPluginNames().add("new-plugin");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}
