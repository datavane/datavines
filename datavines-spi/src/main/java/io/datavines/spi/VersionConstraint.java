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
 * Parses and evaluates version constraints against {@link PluginVersion}.
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

    public static VersionConstraint parse(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Version constraint expression must not be null or empty");
        }

        String trimmed = expression.trim();

        if ("*".equals(trimmed) || "latest".equalsIgnoreCase(trimmed)) {
            return ANY;
        }

        if (isMavenRange(trimmed)) {
            return parseMavenRange(trimmed);
        }

        String[] parts = trimmed.split("\\s+");
        List<Condition> conditions = new ArrayList<Condition>();
        for (String part : parts) {
            conditions.add(parseCondition(part));
        }

        return new VersionConstraint(conditions, trimmed);
    }

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
            return new Condition(Operator.EQ, PluginVersion.of(part.trim()));
        }
    }

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
