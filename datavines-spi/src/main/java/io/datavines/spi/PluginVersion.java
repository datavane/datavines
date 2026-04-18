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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Semantic version in the form {@code MAJOR.MINOR[.PATCH][-qualifier]}.
 */
public final class PluginVersion implements Comparable<PluginVersion> {

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-(.+))?$");

    public static final PluginVersion ZERO = new PluginVersion(0, 0, 0, null);
    public static final PluginVersion MAX = new PluginVersion(999, 999, 999, null);

    private final int major;
    private final int minor;
    private final int patch;
    private final String qualifier; // nullable

    private PluginVersion(int major, int minor, int patch, String qualifier) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.qualifier = qualifier;
    }

    public static PluginVersion of(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string must not be null or empty");
        }
        Matcher m = VERSION_PATTERN.matcher(version.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "Invalid version format: '" + version + "'. Expected MAJOR.MINOR[.PATCH][-qualifier]");
        }
        int major = Integer.parseInt(m.group(1));
        int minor = Integer.parseInt(m.group(2));
        int patch = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
        String qualifier = m.group(4);
        return new PluginVersion(major, minor, patch, qualifier);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String getQualifier() {
        return qualifier;
    }

    public boolean hasQualifier() {
        return qualifier != null;
    }

    public boolean satisfies(VersionConstraint constraint) {
        return constraint.matches(this);
    }

    @Override
    public int compareTo(PluginVersion other) {
        int cmp = Integer.compare(this.major, other.major);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(this.minor, other.minor);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(this.patch, other.patch);
        if (cmp != 0) {
            return cmp;
        }
        // A release sorts after its pre-release variants.
        if (this.qualifier == null && other.qualifier == null) {
            return 0;
        }
        if (this.qualifier == null) {
            return 1;
        }
        if (other.qualifier == null) {
            return -1;
        }
        return this.qualifier.compareTo(other.qualifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PluginVersion that = (PluginVersion) o;
        return major == that.major
                && minor == that.minor
                && patch == that.patch
                && Objects.equals(qualifier, that.qualifier);
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append('.').append(minor).append('.').append(patch);
        if (qualifier != null) {
            sb.append('-').append(qualifier);
        }
        return sb.toString();
    }
}
