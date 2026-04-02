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
 * 语义化版本：MAJOR.MINOR.PATCH[-qualifier]
 *
 * <p>实现 {@link Comparable}，比较顺序：MAJOR → MINOR → PATCH → qualifier（无 qualifier 优先于有 qualifier）。
 * 可安全用作 {@link java.util.TreeMap} 的 Key。
 *
 * <p>示例：
 * <pre>{@code
 * PluginVersion v = PluginVersion.of("8.0.33");
 * PluginVersion v2 = PluginVersion.of("8.0.33-beta1");
 * v.compareTo(v2); // > 0 （无 qualifier 视为正式版，排在 qualifier 之后）
 * }</pre>
 */
public final class PluginVersion implements Comparable<PluginVersion> {

    /**
     * 支持的版本格式：MAJOR.MINOR.PATCH[-qualifier]
     * 其中 MAJOR 和 MINOR 必须，PATCH 可选（默认为 0），qualifier 可选。
     */
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

    /**
     * 从字符串解析版本号。
     *
     * @param version 版本字符串，如 "8.0.33"、"8.0"、"1.0.0-SNAPSHOT"
     * @return 解析后的 PluginVersion
     * @throws IllegalArgumentException 格式不合法
     */
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

    /**
     * 检查是否满足给定的版本约束。
     */
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
        // 无 qualifier（正式版）> 有 qualifier（预发布版）
        if (this.qualifier == null && other.qualifier == null) {
            return 0;
        }
        if (this.qualifier == null) {
            return 1; // 正式版排在预发布版之后（更新）
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
