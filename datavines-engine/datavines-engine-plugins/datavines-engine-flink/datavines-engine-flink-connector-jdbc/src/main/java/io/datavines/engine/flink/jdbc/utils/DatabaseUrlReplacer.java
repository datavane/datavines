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
package io.datavines.engine.flink.jdbc.utils;

import java.util.regex.*;

public class DatabaseUrlReplacer {

    public static String replaceDatabase(String url, String newDb) throws IllegalArgumentException {
        if (url.startsWith("jdbc:mysql:")) {
            return processMysql(url, newDb);
        } else if (url.startsWith("jdbc:postgresql:")) {
            return processPostgresql(url, newDb);
        } else if (url.startsWith("jdbc:sqlserver:")) {
            return processSqlServer(url, newDb);
        } else if (url.startsWith("jdbc:oracle:")) {
            return processOracle(url, newDb);
        } else if (url.startsWith("jdbc:clickhouse:")) {
            return processClickhouse(url, newDb);
        } else if (url.startsWith("jdbc:trino:")) {
            return processTrino(url, newDb);
        } else if (url.startsWith("jdbc:presto:")) {
            return processPresto(url, newDb);
        } else if (url.startsWith("jdbc:hive2:")) {
            return processHive(url, newDb);
        } else if (url.startsWith("jdbc:dm:")) {
            return processDm(url, newDb);
        } else {
            return processMysql(url, newDb);
        }
    }

    private static String processMysql(String url, String newDb) {
        Pattern pattern = Pattern.compile("^(jdbc:mysql://[^/]+)(/?[^?]*)?(\\?.*)?$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String protocol = matcher.group(1);
            String path = matcher.group(2) != null ? matcher.group(2) : "";
            String query = matcher.group(3) != null ? matcher.group(3) : "";
            path = "/" + newDb;
            return protocol + path + query;
        }
        return url;
    }

    private static String processPostgresql(String url, String newDb) {
        return processMysql(url, newDb);
    }

    private static String processSqlServer(String url, String newDb) {
        String[] parts = url.split(";", 2);
        String hostPart = parts[0];
        String params = parts.length > 1 ? parts[1] : "";
        String[] paramsArray = params.split(";");
        StringBuilder newParams = new StringBuilder();
        for (String param : paramsArray) {
            if (param.contains("=")) {
                String[] paramParts = param.split("=", 2);
                if (paramParts[0].equalsIgnoreCase("databaseName")) {
                    newParams.append("databaseName=").append(newDb);
                } else {
                    newParams.append(param);
                }
            }
        }
        return hostPart + ";" + newParams;
    }

    private static String processOracle(String url, String newDb) {
        Pattern sidPattern = Pattern.compile("(jdbc:oracle:thin:@[^:]+:\\d+:)(\\w+)");
        Matcher sidMatcher = sidPattern.matcher(url);
        if (sidMatcher.find()) {
            return sidMatcher.group(1) + newDb;
        }
        Pattern servicePattern = Pattern.compile("(jdbc:oracle:thin:@//[^/]+/)(\\w+)");
        Matcher serviceMatcher = servicePattern.matcher(url);
        if (serviceMatcher.find()) {
            return serviceMatcher.group(1) + newDb;
        }
        return url;
    }

    private static String processClickhouse(String url, String newDb) {
        Pattern pattern = Pattern.compile("(jdbc:clickhouse://[^/]+)(/[^?]*)?(\\?.*)?");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String protocol = matcher.group(1);
            String path = matcher.group(2) != null ? matcher.group(2) : "";
            String query = matcher.group(3) != null ? matcher.group(3) : "";
            path = "/" + newDb;
            query = query.replaceAll("([?&])database=[^&]*", "");
            return protocol + path + query;
        }
        return url;
    }

    private static String processTrino(String url, String newDb) {
        return url.replaceAll("(jdbc:trino://[^/]+(/[^/]+)*/)([^/?#]+)", "$1" + newDb);
    }

    private static String processPresto(String url, String newDb) {
        return url.replaceAll("(jdbc:presto://[^/]+(/[^/]+)*/)([^/?#]+)", "$1" + newDb);
    }

    private static String processHive(String url, String newDb) {
        return url.replaceAll("(jdbc:hive2://[^/]+/)([^/?]*)", "$1" + newDb);
    }

    private static String processDm(String url, String newDb) {
        String[] parts = url.split(";", 2);
        String hostPart = parts[0];
        String params = parts.length > 1 ? parts[1] : "";
        String[] paramsArray = params.split("\\?");
        StringBuilder newParams = new StringBuilder();
        for (String param : paramsArray) {
            if (param.contains("=")) {
                String[] paramParts = param.split("=", 2);
                if (paramParts[0].equalsIgnoreCase("schema")) {
                    newParams.append("schema=").append(newDb);
                } else {
                    newParams.append(param);
                }
            }
        }
        return hostPart + ";" + newParams;
    }
}
