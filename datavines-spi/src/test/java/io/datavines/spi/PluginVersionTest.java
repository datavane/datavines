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

public class PluginVersionTest {

    @Test
    public void testParseFullVersion() {
        PluginVersion v = PluginVersion.of("8.0.33");
        assertEquals(8, v.getMajor());
        assertEquals(0, v.getMinor());
        assertEquals(33, v.getPatch());
        assertNull(v.getQualifier());
        assertFalse(v.hasQualifier());
        assertEquals("8.0.33", v.toString());
    }

    @Test
    public void testParseTwoPartVersion() {
        PluginVersion v = PluginVersion.of("5.7");
        assertEquals(5, v.getMajor());
        assertEquals(7, v.getMinor());
        assertEquals(0, v.getPatch());
        assertEquals("5.7.0", v.toString());
    }

    @Test
    public void testParseWithQualifier() {
        PluginVersion v = PluginVersion.of("1.0.0-SNAPSHOT");
        assertEquals(1, v.getMajor());
        assertEquals(0, v.getMinor());
        assertEquals(0, v.getPatch());
        assertEquals("SNAPSHOT", v.getQualifier());
        assertTrue(v.hasQualifier());
        assertEquals("1.0.0-SNAPSHOT", v.toString());
    }

    @Test
    public void testParseWithBetaQualifier() {
        PluginVersion v = PluginVersion.of("2.1.0-beta1");
        assertEquals("beta1", v.getQualifier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidVersion() {
        PluginVersion.of("abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() {
        PluginVersion.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmpty() {
        PluginVersion.of("");
    }

    @Test
    public void testCompareByMajor() {
        assertTrue(PluginVersion.of("2.0.0").compareTo(PluginVersion.of("1.0.0")) > 0);
    }

    @Test
    public void testCompareByMinor() {
        assertTrue(PluginVersion.of("1.2.0").compareTo(PluginVersion.of("1.1.0")) > 0);
    }

    @Test
    public void testCompareByPatch() {
        assertTrue(PluginVersion.of("1.0.2").compareTo(PluginVersion.of("1.0.1")) > 0);
    }

    @Test
    public void testCompareEqual() {
        assertEquals(0, PluginVersion.of("1.0.0").compareTo(PluginVersion.of("1.0.0")));
    }

    @Test
    public void testReleaseBeatsPreRelease() {
        // 正式版 (无 qualifier) 应排在预发布版 (有 qualifier) 之后（更大）
        PluginVersion release = PluginVersion.of("1.0.0");
        PluginVersion snapshot = PluginVersion.of("1.0.0-SNAPSHOT");
        assertTrue(release.compareTo(snapshot) > 0);
    }

    @Test
    public void testQualifierComparison() {
        PluginVersion alpha = PluginVersion.of("1.0.0-alpha");
        PluginVersion beta = PluginVersion.of("1.0.0-beta");
        assertTrue(beta.compareTo(alpha) > 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        PluginVersion v1 = PluginVersion.of("8.0.33");
        PluginVersion v2 = PluginVersion.of("8.0.33");
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void testNotEquals() {
        assertNotEquals(PluginVersion.of("8.0.33"), PluginVersion.of("8.0.34"));
    }

    @Test
    public void testZeroAndMax() {
        assertEquals(0, PluginVersion.ZERO.getMajor());
        assertEquals(999, PluginVersion.MAX.getMajor());
        assertTrue(PluginVersion.MAX.compareTo(PluginVersion.ZERO) > 0);
    }

    @Test
    public void testTwoPartDefaultsPatchToZero() {
        assertEquals(PluginVersion.of("5.7"), PluginVersion.of("5.7.0"));
    }
}
