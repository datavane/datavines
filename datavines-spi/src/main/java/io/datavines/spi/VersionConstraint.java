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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 版本约束表达式，用于匹配 {@link PluginVersion}。
 *
 * <p>支持的语法：
 * <ul>
 *   <li>{@code "8.0.33"} — 精确匹配</li>
 *   <li>{@code ">=8.0.0"} — 大于等于</li>
 *   <li>{@code ">8.0.0"} — 严格大于</li>
 *   <li>{@code "<=9.0.0"} — 小于等于</li>
 *   <li>{@code "<9.0.0"} — 严格小于</li>
 *   <li>{@code ">=8.0.0 <9.0.0"} — 范围（空格分隔多个条件，AND 语义）</li>
 *   <li>{@code "*"} 或 {@code "latest"} — 任意版本</li>
 * </ul>
 *
 * <p>也支持 Maven 风格的范围表达式：{@code "[1.0.0,2.0.0)"} 表示 {@code >=1.0.0 <2.0.0}。
 */
public final class VersionConstraint {

    public static final VersionConstraint ANY = new VersionConstraint(
            Collections.<Condition>emptyList(), "*");

    private final List<Condition> conditions;
    private final String expression;

    private VersionConstraint(List<Condition> conditions, String expression) {
        this.conditions = Collections.unmodifiableList(conditions);
        this.expression = expression;
    }

    /**
     * 解析版本约束表达式。
     *
     * @param expression 约束表达式
     * @return 解析后的 VersionConstraint
     * @throws IllegalArgumentException 表达式格式非法
     */
    public static VersionConstraint parse(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Version constraint expression must not be null or empty");
        }

        String trimmed = expression.trim();

        // 通配符
        if ("*".equals(trimmed) || "latest".equalsIgnoreCase(trimmed)) {
            return ANY;
        }

        // Maven 风格范围：[1.0.0,2.0.0) 或 (1.0.0,2.0.0]
        if (isMavenRange(trimmed)) {
            return parseMavenRange(trimmed);
        }

        // 空格分隔的多条件
        String[] parts = trimmed.split("\\s+");
        List<Condition> conditions = new ArrayList<Condition>();
        for (String part : parts) {
            conditions.add(parseCondition(part));
        }

        return new VersionConstraint(conditions, trimmed);
    }

    /**
     * 检查给定版本是否满足此约束。
     */
    public boolean matches(PluginVersion version) {
        if (conditions.isEmpty()) {
            return true; // ANY
        }
        for (Condition condition : conditions) {
            if (!condition.test(version)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从满足约束的版本集合中选出最新的。
     *
     * @param candidates 候选版本集合
     * @return 满足约束的最新版本，或空
     */
    public PluginVersion selectLatest(Collection<PluginVersion> candidates) {
        PluginVersion best = null;
        for (PluginVersion v : candidates) {
            if (matches(v)) {
                if (best == null || v.compareTo(best) > 0) {
                    best = v;
                }
            }
        }
        return best;
    }

    @Override
    public String toString() {
        return expression;
    }

    // ── Maven 范围解析 ──────────────────────────────────────────

    private static boolean isMavenRange(String expr) {
        return (expr.startsWith("[") || expr.startsWith("("))
                && (expr.endsWith("]") || expr.endsWith(")"))
                && expr.contains(",");
    }

    private static VersionConstraint parseMavenRange(String expr) {
        boolean lowerInclusive = expr.charAt(0) == '[';
        boolean upperInclusive = expr.charAt(expr.length() - 1) == ']';

        String inner = expr.substring(1, expr.length() - 1);
        String[] bounds = inner.split(",", 2);
        if (bounds.length != 2) {
            throw new IllegalArgumentException("Invalid Maven range: " + expr);
        }

        List<Condition> conditions = new ArrayList<Condition>();

        String lower = bounds[0].trim();
        if (!lower.isEmpty()) {
            PluginVersion lv = PluginVersion.of(lower);
            conditions.add(lowerInclusive ? new Condition(Operator.GTE, lv) : new Condition(Operator.GT, lv));
        }

        String upper = bounds[1].trim();
        if (!upper.isEmpty()) {
            PluginVersion uv = PluginVersion.of(upper);
            conditions.add(upperInclusive ? new Condition(Operator.LTE, uv) : new Condition(Operator.LT, uv));
        }

        return new VersionConstraint(conditions, expr);
    }

    // ── 单条件解析 ──────────────────────────────────────────────

    private static Condition parseCondition(String part) {
        if (part.startsWith(">=")) {
            return new Condition(Operator.GTE, PluginVersion.of(part.substring(2).trim()));
        } else if (part.startsWith(">")) {
            return new Condition(Operator.GT, PluginVersion.of(part.substring(1).trim()));
        } else if (part.startsWith("<=")) {
            return new Condition(Operator.LTE, PluginVersion.of(part.substring(2).trim()));
        } else if (part.startsWith("<")) {
            return new Condition(Operator.LT, PluginVersion.of(part.substring(1).trim()));
        } else if (part.startsWith("=")) {
            return new Condition(Operator.EQ, PluginVersion.of(part.substring(1).trim()));
        } else {
            // 无操作符 → 精确匹配
            return new Condition(Operator.EQ, PluginVersion.of(part.trim()));
        }
    }

    // ── 内部类 ──────────────────────────────────────────────────

    private enum Operator {
        EQ, GT, GTE, LT, LTE
    }

    private static final class Condition {
        private final Operator operator;
        private final PluginVersion target;

        Condition(Operator operator, PluginVersion target) {
            this.operator = operator;
            this.target = target;
        }

        boolean test(PluginVersion version) {
            int cmp = version.compareTo(target);
            switch (operator) {
                case EQ:  return cmp == 0;
                case GT:  return cmp > 0;
                case GTE: return cmp >= 0;
                case LT:  return cmp < 0;
                case LTE: return cmp <= 0;
                default:  return false;
            }
        }
    }
}
