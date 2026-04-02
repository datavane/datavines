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

import static org.junit.Assert.*;

public class PluginDescriptorTest {

    @Test
    public void testOf() {
        PluginDescriptor desc = PluginDescriptor.of("mysql", "8.0.33");
        assertEquals("mysql", desc.getPluginName());
        assertEquals(PluginVersion.of("8.0.33"), desc.getVersion());
        assertEquals("mysql@8.0.33", desc.getPluginId());
        assertEquals(PluginVersion.ZERO, desc.getSpiVersion());
        assertEquals("", desc.getMainVersionRange());
        assertEquals("", desc.getDescription());
    }

    @Test
    public void testOfFull() {
        PluginDescriptor desc = PluginDescriptor.of(
                "mysql", "8.0.33", "1.0.0",
                "[1.0.0,2.0.0)", "MySQL Connector");
        assertEquals("mysql", desc.getPluginName());
        assertEquals(PluginVersion.of("8.0.33"), desc.getVersion());
        assertEquals(PluginVersion.of("1.0.0"), desc.getSpiVersion());
        assertEquals("[1.0.0,2.0.0)", desc.getMainVersionRange());
        assertEquals("MySQL Connector", desc.getDescription());
    }

    @Test
    public void testPluginId() {
        PluginDescriptor desc = PluginDescriptor.of("postgresql", "42.7.0");
        assertEquals("postgresql@42.7.0", desc.getPluginId());
    }

    @Test
    public void testCompatibilityNoRange() {
        PluginDescriptor desc = PluginDescriptor.of("mysql", "8.0.33");
        // 未声明版本范围时应返回 compatible
        assertTrue(desc.isCompatibleWith(PluginVersion.of("1.0.0")));
        assertTrue(desc.isCompatibleWith(PluginVersion.of("99.0.0")));
    }

    @Test
    public void testCompatibilityWithRange() {
        PluginDescriptor desc = PluginDescriptor.of(
                "mysql", "8.0.33", "1.0.0",
                "[1.0.0,2.0.0)", "MySQL Connector");

        assertTrue(desc.isCompatibleWith(PluginVersion.of("1.0.0")));
        assertTrue(desc.isCompatibleWith(PluginVersion.of("1.5.0")));
        assertFalse(desc.isCompatibleWith(PluginVersion.of("2.0.0")));
        assertFalse(desc.isCompatibleWith(PluginVersion.of("0.9.0")));
    }

    @Test
    public void testToString() {
        PluginDescriptor desc = PluginDescriptor.of("mysql", "8.0.33");
        String str = desc.toString();
        assertTrue(str.contains("mysql@8.0.33"));
    }
}
