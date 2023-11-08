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
package io.datavines.engine.spark.jdbc.source;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.spark.api.SparkRuntimeEnvironment;
import io.datavines.engine.spark.api.batch.SparkBatchSource;
import org.apache.spark.sql.SparkSession;

import static io.datavines.common.ConfigConstants.*;

public class MongodbSource implements SparkBatchSource {

    private Config config = new Config();

    @Override
    public void setConfig(Config config) {
        if(config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {

        List<String> nonExistsOptions = Stream.of(SPARK_MONGODB_INPUT_URI, SPARK_MONGODB_INPUT_COLLECTION)
                .filter(keyOptions -> !config.has(keyOptions))
                .collect(Collectors.toList());

        if (!nonExistsOptions.isEmpty()) {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new CheckResult(true, "");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment prepareEnv) {

    }

    @Override
    public Dataset<Row> getData(SparkRuntimeEnvironment env) {

        SparkSession sparkSession = env.sparkSession();

        DataFrameReader read = sparkSession.read();
        read.option(SPARK_MONGODB_INPUT_URI, config.getString(SPARK_MONGODB_INPUT_URI));
        read.option(SPARK_MONGODB_INPUT_COLLECTION, config.getString(SPARK_MONGODB_INPUT_COLLECTION));

        return read.format("mongo").load();
    }


}
