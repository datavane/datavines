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
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.spark.api.SparkRuntimeEnvironment;
import io.datavines.engine.spark.api.batch.SparkBatchSink;

import static io.datavines.common.ConfigConstants.*;

@Slf4j
public class JdbcSink implements SparkBatchSink {

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
        List<String> requiredOptions = Arrays.asList(URL, TABLE, USER, PASSWORD);

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x->{
            if(!config.has(x)){
                nonExistsOptions.add(x);
            }
        });

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
        if (StringUtils.isEmptyOrNullStr(config.getString(SAVE_MODE))) {
            config.put(SAVE_MODE, SaveMode.Append);
        }
    }

    @Override
    public Void output(Dataset<Row> data, SparkRuntimeEnvironment environment) {
        if (UPSERT.equals(config.getString(SAVE_MODE))) {
            data = environment.sparkSession().sql("select * from invalidate_count_" + config.getString(METRIC_UNIQUE_KEY));
            Properties prop = new Properties();
            prop.setProperty(DRIVER, config.getString(DRIVER));
            prop.setProperty(USER, config.getString(USER));
            prop.setProperty(PASSWORD, config.getString(PASSWORD));
            String url = config.getString(URL);
            String sql = config.getString(SQL);

            List<Row> rows = data.takeAsList(50);
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(url, prop);
                PreparedStatement pstmt = connection.prepareStatement(sql);
                if (rows.size() == 1) {
                    Row rowValue = rows.get(0);
                    if (rowValue.isNullAt(0)) {
                        pstmt.setObject(1, 0);
                        pstmt.executeUpdate();
                    } else {
                        pstmt.setObject(1, rowValue.get(0));
                        pstmt.executeUpdate();
                    }
                } else if ((rows.size() > 1)) {
                    List<String> resultList = new ArrayList<>();
                    for (Row row : rows) {
                        resultList.add(row.get(0).toString());
                    }

                    pstmt.setObject(1, String.join("@#@",resultList.toArray(new String[]{})));
                    pstmt.executeUpdate();
                }

            } catch (SQLException e) {
                log.error("execute sql error", e);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception e) {
                        log.error("close connection error", e);
                    }
                }
            }

        } else {
            if (!Strings.isNullOrEmpty(config.getString(SQL))) {
                data = environment.sparkSession().sql(config.getString(SQL));
            }

            String saveMode = config.getString(SAVE_MODE);

            Properties prop = new Properties();
            prop.setProperty(DRIVER, config.getString(DRIVER));
            prop.setProperty(USER, config.getString(USER));
            prop.setProperty(PASSWORD, config.getString(PASSWORD));
            data.write().mode(saveMode).jdbc(config.getString(URL), config.getString(TABLE), prop);
        }

        return null;
    }

    @Override
    public java.util.Collection<String> getPluginNames() {
        return java.util.Arrays.asList("livy-batch-jdbc-sink", "spark-batch-jdbc-sink");
    }
}
