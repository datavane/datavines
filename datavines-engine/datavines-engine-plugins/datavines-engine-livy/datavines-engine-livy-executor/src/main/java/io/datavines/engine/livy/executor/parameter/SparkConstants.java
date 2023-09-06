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
package io.datavines.engine.livy.executor.parameter;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SparkConstants {

    private SparkConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String DEFAULT = "default";

    public static final List<String> jars = Stream.of(
            "datavines-common-1.0.0-SNAPSHOT.jar",
            "datavines-spi-1.0.0-SNAPSHOT.jar",
            "datavines-engine-spark-api-1.0.0-SNAPSHOT.jar",
            "datavines-engine-spark-connector-jdbc-1.0.0-SNAPSHOT.jar",
            "datavines-engine-core-1.0.0-SNAPSHOT.jar",
            "datavines-engine-spark-transform-sql-1.0.0-SNAPSHOT.jar",
            "datavines-engine-api-1.0.0-SNAPSHOT.jar",
            "mysql-connector-java-8.0.16.jar",
            "httpclient-4.4.1.jar",
            "httpcore-4.4.1.jar",
            "postgresql-42.2.6.jar",
            "presto-jdbc-0.238.jar",
            "trino-jdbc-407.jar",
            "clickhouse-jdbc-0.1.53.jar"
    ).collect(Collectors.toList());


}
