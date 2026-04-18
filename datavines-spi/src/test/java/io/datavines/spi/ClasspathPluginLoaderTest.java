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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link ClasspathPluginLoader}, focusing on the DescriptorIndex
 * dual-matching strategy (name-based + class-origin-based).
 */
public class ClasspathPluginLoaderTest {

    // ---------------------------------------------------------------------------
    // Fake SPI interface for testing
    // ---------------------------------------------------------------------------

    interface FakeSpi {
        String getPluginName();
    }

    /** Single-key plugin — plugin.name matches getPluginName() directly. */
    static class SingleKeyPlugin implements FakeSpi {
        @Override
        public String getPluginName() {
            return "single-plugin";
        }
    }

    /** Multi-key plugin — registered under two names, neither matches descriptor name. */
    interface MultiKeySpi {
        Collection<String> getPluginNames();
    }

    static class MultiKeyPlugin implements MultiKeySpi {
        @Override
        public Collection<String> getPluginNames() {
            return Arrays.asList("multi_key_a", "multi_key_b");
        }
    }

    // ---------------------------------------------------------------------------
    // DescriptorIndex tests
    // ---------------------------------------------------------------------------

    @Test
    public void testNameMatchReturnsDescriptor() {
        PluginDescriptor desc = PluginDescriptor.of("single-plugin", "1.0.0");
        Map<String, List<PluginDescriptor>> byName = new LinkedHashMap<String, List<PluginDescriptor>>();
        byName.put("single-plugin", Collections.singletonList(desc));
        Map<String, PluginDescriptor> byUrl = new LinkedHashMap<String, PluginDescriptor>();

        ClasspathPluginLoader.DescriptorIndex index =
                new ClasspathPluginLoader.DescriptorIndex(byName, byUrl);

        SingleKeyPlugin plugin = new SingleKeyPlugin();
        PluginDescriptor found = index.findDescriptor("single-plugin", plugin);

        assertNotNull(found);
        assertEquals("single-plugin", found.getPluginName());
        assertEquals(PluginVersion.of("1.0.0"), found.getVersion());
    }

    @Test
    public void testMissingDescriptorCreatesDefault() {
        // No descriptors registered at all
        Map<String, List<PluginDescriptor>> byName = new LinkedHashMap<String, List<PluginDescriptor>>();
        Map<String, PluginDescriptor> byUrl = new LinkedHashMap<String, PluginDescriptor>();
        ClasspathPluginLoader.DescriptorIndex index =
                new ClasspathPluginLoader.DescriptorIndex(byName, byUrl);

        SingleKeyPlugin plugin = new SingleKeyPlugin();
        PluginDescriptor found = index.findDescriptor("unknown-plugin", plugin);

        // Must create a default descriptor (never return null)
        assertNotNull(found);
        assertEquals("unknown-plugin", found.getPluginName());
        // Default version should be non-null
        assertNotNull(found.getVersion());
    }

    @Test
    public void testOriginMatchWhenNameDoesNotMatch() {
        // Simulate a multi-key plugin whose descriptor name ("module-config") differs
        // from its registered names ("engine_task_a", "engine_task_b").
        // The origin-based match should still find the descriptor.

        MultiKeyPlugin plugin = new MultiKeyPlugin();

        // Get the actual code-source URL of this test class as the "origin"
        // (In production the provider class comes from the plugin JAR; in tests
        //  it comes from target/test-classes/, which we can simulate.)
        String classOrigin = getCodeSourceUrl(plugin.getClass());
        // classOrigin might be null in some test environments — skip if so
        if (classOrigin == null) {
            return; // cannot test origin matching without ProtectionDomain
        }

        // Build a descriptor URL that starts with classOrigin
        PluginDescriptor desc = PluginDescriptor.of("module-config", "1.0.0");
        Map<String, List<PluginDescriptor>> byName = new LinkedHashMap<String, List<PluginDescriptor>>();
        // Intentionally NOT adding "engine_task_a" or "engine_task_b" — they won't name-match
        Map<String, PluginDescriptor> byUrl = new LinkedHashMap<String, PluginDescriptor>();
        // descriptor URL = classOrigin + "META-INF/datavines-plugin.properties"
        String descriptorUrl = classOrigin + "META-INF/datavines-plugin.properties";
        byUrl.put(descriptorUrl, desc);

        ClasspathPluginLoader.DescriptorIndex index =
                new ClasspathPluginLoader.DescriptorIndex(byName, byUrl);

        // Neither "engine_task_a" nor "engine_task_b" is in byName,
        // but origin match should find "module-config" descriptor.
        PluginDescriptor foundA = index.findDescriptor("engine_task_a", plugin);
        PluginDescriptor foundB = index.findDescriptor("engine_task_b", plugin);

        assertNotNull(foundA);
        assertEquals("module-config", foundA.getPluginName());
        assertNotNull(foundB);
        assertEquals("module-config", foundB.getPluginName());
    }

    @Test
    public void testNameMatchTakesPrecedenceOverOriginMatch() {
        MultiKeyPlugin plugin = new MultiKeyPlugin();
        String classOrigin = getCodeSourceUrl(plugin.getClass());
        if (classOrigin == null) {
            return;
        }

        // Both name-match and origin-match descriptors registered
        PluginDescriptor nameDesc = PluginDescriptor.of("multi_key_a", "2.0.0");
        PluginDescriptor originDesc = PluginDescriptor.of("module-config", "1.0.0");

        Map<String, List<PluginDescriptor>> byName = new LinkedHashMap<String, List<PluginDescriptor>>();
        byName.put("multi_key_a", Collections.singletonList(nameDesc));

        Map<String, PluginDescriptor> byUrl = new LinkedHashMap<String, PluginDescriptor>();
        byUrl.put(classOrigin + "META-INF/datavines-plugin.properties", originDesc);

        ClasspathPluginLoader.DescriptorIndex index =
                new ClasspathPluginLoader.DescriptorIndex(byName, byUrl);

        // For "multi_key_a", name-match descriptor (2.0.0) should win
        PluginDescriptor found = index.findDescriptor("multi_key_a", plugin);
        assertNotNull(found);
        assertEquals("multi_key_a", found.getPluginName());
        assertEquals(PluginVersion.of("2.0.0"), found.getVersion());

        // For "multi_key_b" (not in byName), origin-match descriptor (1.0.0) should be used
        PluginDescriptor foundB = index.findDescriptor("multi_key_b", plugin);
        assertNotNull(foundB);
        assertEquals("module-config", foundB.getPluginName());
        assertEquals(PluginVersion.of("1.0.0"), foundB.getVersion());
    }

    @Test
    public void testDuplicatePluginNamesUseOriginMatch() {
        SingleKeyPlugin plugin = new SingleKeyPlugin();
        String classOrigin = getCodeSourceUrl(plugin.getClass());
        if (classOrigin == null) {
            return;
        }

        PluginDescriptor v1 = PluginDescriptor.of("single-plugin", "1.0.0");
        PluginDescriptor v2 = PluginDescriptor.of("single-plugin", "2.0.0");

        Map<String, List<PluginDescriptor>> byName = new LinkedHashMap<String, List<PluginDescriptor>>();
        byName.put("single-plugin", Arrays.asList(v1, v2));

        Map<String, PluginDescriptor> byUrl = new LinkedHashMap<String, PluginDescriptor>();
        byUrl.put(classOrigin + "META-INF/datavines-plugin.properties", v1);

        ClasspathPluginLoader.DescriptorIndex index =
                new ClasspathPluginLoader.DescriptorIndex(byName, byUrl);

        PluginDescriptor found = index.findDescriptor("single-plugin", plugin);
        assertNotNull(found);
        assertEquals(PluginVersion.of("1.0.0"), found.getVersion());
    }

    @Test
    public void testDuplicatePluginNamesWithoutOriginUseLatestVersion() {
        SingleKeyPlugin plugin = new SingleKeyPlugin();

        PluginDescriptor v1 = PluginDescriptor.of("single-plugin", "1.0.0");
        PluginDescriptor v2 = PluginDescriptor.of("single-plugin", "2.0.0");

        Map<String, List<PluginDescriptor>> byName = new LinkedHashMap<String, List<PluginDescriptor>>();
        byName.put("single-plugin", Arrays.asList(v1, v2));

        Map<String, PluginDescriptor> byUrl = new LinkedHashMap<String, PluginDescriptor>();

        ClasspathPluginLoader.DescriptorIndex index =
                new ClasspathPluginLoader.DescriptorIndex(byName, byUrl);

        PluginDescriptor found = index.findDescriptor("single-plugin", plugin);
        assertNotNull(found);
        assertEquals(PluginVersion.of("2.0.0"), found.getVersion());
    }

    @Test
    public void testPluginDiscoveryClasspathModeDoesNotThrow() {
        // Verify that PluginDiscovery.getMultiKeyPluginDiscovery does not throw
        // when no bootstrap is initialized (simulates IDEA/classpath mode).
        // The fallback to ClasspathPluginLoader or legacy ServiceLoader must succeed
        // (even if no actual implementations are found for FakeSpi in this test classpath).
        PluginDiscovery.resetPluginDiscovery(FakeSpi.class);

        // Should not throw — may return empty discovery
        PluginDiscovery<FakeSpi> discovery = PluginDiscovery.getMultiKeyPluginDiscovery(
                FakeSpi.class,
                p -> Arrays.asList(p.getPluginName()));

        assertNotNull(discovery);
        // getSupportedPlugins() should not throw
        assertNotNull(discovery.getSupportedPlugins());

        PluginDiscovery.resetPluginDiscovery(FakeSpi.class);
    }

    @Test
    public void testPluginDiscoveryUpgradesToBootstrapRegistry() {
        PluginDiscovery.resetPluginDiscovery(FakeSpi.class);
        PluginDiscoveryBootstrap.reset();

        PluginDiscovery<FakeSpi> initial = PluginDiscovery.getPluginDiscovery(
                FakeSpi.class,
                FakeSpi::getPluginName);
        assertNotNull(initial);

        VersionedPluginRegistry<FakeSpi> registry =
                VersionedPluginRegistry.<FakeSpi>builder("FakeSpi")
                        .register(PluginDescriptor.of("single-plugin", "3.0.0"), new SingleKeyPlugin())
                        .build();

        Map<Class<?>, VersionedPluginRegistry<?>> registries = new HashMap<Class<?>, VersionedPluginRegistry<?>>();
        registries.put(FakeSpi.class, registry);
        PluginDiscoveryBootstrap.initialize(registries);

        PluginDiscovery<FakeSpi> upgraded = PluginDiscovery.getPluginDiscovery(
                FakeSpi.class,
                FakeSpi::getPluginName);
        assertNotNull(upgraded.getVersionedDiscovery());
        assertTrue(upgraded.hasPlugin("single-plugin", "3.0.0"));

        PluginDiscovery.resetPluginDiscovery(FakeSpi.class);
        PluginDiscoveryBootstrap.reset();
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private static String getCodeSourceUrl(Class<?> clazz) {
        try {
            java.security.ProtectionDomain pd = clazz.getProtectionDomain();
            if (pd == null) return null;
            java.security.CodeSource cs = pd.getCodeSource();
            if (cs == null || cs.getLocation() == null) return null;
            return cs.getLocation().toString();
        } catch (SecurityException e) {
            return null;
        }
    }
}
