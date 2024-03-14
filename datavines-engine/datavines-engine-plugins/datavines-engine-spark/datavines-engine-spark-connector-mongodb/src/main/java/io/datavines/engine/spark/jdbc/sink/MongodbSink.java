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
package io.datavines.engine.spark.jdbc.sink;

import io.datavines.common.utils.StringUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.spark.api.SparkRuntimeEnvironment;
import io.datavines.engine.spark.api.batch.SparkBatchSink;

import static io.datavines.common.ConfigConstants.*;

public class MongodbSink implements SparkBatchSink {

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

        List<String> nonExistsOptions = Stream.of(SPARK_MONGODB_OUTPUT_URI, SPARK_MONGODB_OUTPUT_COLLECTION)
                .filter(keyOptions -> !config.has(keyOptions))
                .collect(Collectors.toList());

        if (nonExistsOptions.isEmpty()) {

            return new CheckResult(true, "");
        } else {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment prepareEnv) {
        if (StringUtils.isEmptyOrNullStr(config.getString(SAVE_MODE))) {
            config.put(SAVE_MODE, SaveMode.Append);
        }
    }

    @Override
    public Void output(Dataset<Row> data, SparkRuntimeEnvironment environment) {

        if (!Strings.isNullOrEmpty(config.getString(SQL))) {
            data = environment.sparkSession().sql(config.getString(SQL));
        }

        String saveMode = config.getString(SAVE_MODE);

        data.write().format("mongo")
                .mode(saveMode)
                .option(SPARK_MONGODB_OUTPUT_URI, config.getString(SPARK_MONGODB_OUTPUT_URI))
                .option("collection", config.getString(SPARK_MONGODB_OUTPUT_COLLECTION)).save();

        return null;
    }
}
