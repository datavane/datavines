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
import java.util.List;

import static org.junit.Assert.*;

public class VersionConstraintTest {

    @Test
    public void testExactMatch() {
        VersionConstraint c = VersionConstraint.parse("8.0.33");
        assertTrue(c.matches(PluginVersion.of("8.0.33")));
        assertFalse(c.matches(PluginVersion.of("8.0.34")));
        assertFalse(c.matches(PluginVersion.of("7.0.0")));
    }

    @Test
    public void testGreaterThanOrEqual() {
        VersionConstraint c = VersionConstraint.parse(">=8.0.0");
        assertTrue(c.matches(PluginVersion.of("8.0.0")));
        assertTrue(c.matches(PluginVersion.of("8.0.1")));
        assertTrue(c.matches(PluginVersion.of("9.0.0")));
        assertFalse(c.matches(PluginVersion.of("7.9.9")));
    }

    @Test
    public void testGreaterThan() {
        VersionConstraint c = VersionConstraint.parse(">8.0.0");
        assertFalse(c.matches(PluginVersion.of("8.0.0")));
        assertTrue(c.matches(PluginVersion.of("8.0.1")));
    }

    @Test
    public void testLessThan() {
        VersionConstraint c = VersionConstraint.parse("<9.0.0");
        assertTrue(c.matches(PluginVersion.of("8.9.9")));
        assertFalse(c.matches(PluginVersion.of("9.0.0")));
    }

    @Test
    public void testLessThanOrEqual() {
        VersionConstraint c = VersionConstraint.parse("<=9.0.0");
        assertTrue(c.matches(PluginVersion.of("9.0.0")));
        assertFalse(c.matches(PluginVersion.of("9.0.1")));
    }

    @Test
    public void testRangeExpression() {
        VersionConstraint c = VersionConstraint.parse(">=8.0.0 <9.0.0");
        assertTrue(c.matches(PluginVersion.of("8.0.0")));
        assertTrue(c.matches(PluginVersion.of("8.5.0")));
        assertFalse(c.matches(PluginVersion.of("9.0.0")));
        assertFalse(c.matches(PluginVersion.of("7.9.9")));
    }

    @Test
    public void testWildcard() {
        VersionConstraint c = VersionConstraint.parse("*");
        assertTrue(c.matches(PluginVersion.of("1.0.0")));
        assertTrue(c.matches(PluginVersion.of("999.0.0")));
    }

    @Test
    public void testLatest() {
        VersionConstraint c = VersionConstraint.parse("latest");
        assertTrue(c.matches(PluginVersion.of("1.0.0")));
    }

    @Test
    public void testAnyConstant() {
        assertTrue(VersionConstraint.ANY.matches(PluginVersion.of("1.0.0")));
        assertTrue(VersionConstraint.ANY.matches(PluginVersion.of("99.0.0")));
    }

    @Test
    public void testMavenRangeInclusive() {
        // [1.0.0,2.0.0] → >=1.0.0 <=2.0.0
        VersionConstraint c = VersionConstraint.parse("[1.0.0,2.0.0]");
        assertTrue(c.matches(PluginVersion.of("1.0.0")));
        assertTrue(c.matches(PluginVersion.of("1.5.0")));
        assertTrue(c.matches(PluginVersion.of("2.0.0")));
        assertFalse(c.matches(PluginVersion.of("0.9.0")));
        assertFalse(c.matches(PluginVersion.of("2.0.1")));
    }

    @Test
    public void testMavenRangeExclusiveUpper() {
        // [1.0.0,2.0.0) → >=1.0.0 <2.0.0
        VersionConstraint c = VersionConstraint.parse("[1.0.0,2.0.0)");
        assertTrue(c.matches(PluginVersion.of("1.0.0")));
        assertTrue(c.matches(PluginVersion.of("1.9.9")));
        assertFalse(c.matches(PluginVersion.of("2.0.0")));
    }

    @Test
    public void testMavenRangeExclusiveLower() {
        // (1.0.0,2.0.0] → >1.0.0 <=2.0.0
        VersionConstraint c = VersionConstraint.parse("(1.0.0,2.0.0]");
        assertFalse(c.matches(PluginVersion.of("1.0.0")));
        assertTrue(c.matches(PluginVersion.of("1.0.1")));
        assertTrue(c.matches(PluginVersion.of("2.0.0")));
    }

    @Test
    public void testSelectLatest() {
        VersionConstraint c = VersionConstraint.parse(">=8.0.0 <9.0.0");
        List<PluginVersion> candidates = Arrays.asList(
                PluginVersion.of("7.0.0"),
                PluginVersion.of("8.0.0"),
                PluginVersion.of("8.0.33"),
                PluginVersion.of("9.0.0")
        );

        PluginVersion selected = c.selectLatest(candidates);
        assertNotNull(selected);
        assertEquals(PluginVersion.of("8.0.33"), selected);
    }

    @Test
    public void testSelectLatestNoMatch() {
        VersionConstraint c = VersionConstraint.parse(">=10.0.0");
        List<PluginVersion> candidates = Arrays.asList(
                PluginVersion.of("8.0.0"),
                PluginVersion.of("9.0.0")
        );

        PluginVersion selected = c.selectLatest(candidates);
        assertNull(selected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() {
        VersionConstraint.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmpty() {
        VersionConstraint.parse("");
    }

    @Test
    public void testExplicitEquals() {
        VersionConstraint c = VersionConstraint.parse("=8.0.0");
        assertTrue(c.matches(PluginVersion.of("8.0.0")));
        assertFalse(c.matches(PluginVersion.of("8.0.1")));
    }
}
