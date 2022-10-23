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
package io.datavines.engine.jdbc.file.connector;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.common.config.enums.SinkType;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.StringUtils;
import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.jdbc.api.JdbcRuntimeEnvironment;
import io.datavines.engine.jdbc.api.JdbcSink;
import io.datavines.engine.jdbc.api.entity.ResultList;
import io.datavines.engine.jdbc.api.utils.FileUtils;
import io.datavines.engine.jdbc.api.utils.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static io.datavines.engine.api.ConfigConstants.EXPECTED_VALUE;
import static io.datavines.engine.api.ConfigConstants.JOB_EXECUTION_ID;
import static io.datavines.engine.api.ConfigConstants.SQL;
import static io.datavines.engine.api.EngineConstants.PLUGIN_TYPE;

public class FileSink implements JdbcSink {

    private Logger logger = LoggerFactory.getLogger(FileSink.class);

    private Config config = new Config();

    @Override
    public void output(List<ResultList> resultList, JdbcRuntimeEnvironment env) {

        Map<String,String> inputParameter = new HashMap<>();
        setExceptedValue(config, resultList, inputParameter);

        String validateResultDataDir = config.getString("result_data_file_dir") + File.separator + config.getString(JOB_EXECUTION_ID);

        switch (SinkType.of(config.getString(PLUGIN_TYPE))){
            case ERROR_DATA:
                break;
            case ACTUAL_VALUE:
            case TASK_RESULT:
                String sql = config.getString(SQL);
                sql = PlaceholderUtils.replacePlaceholders(sql, inputParameter,true);
                FileUtils.writeToLocal(parseSqlToList(sql), validateResultDataDir,config.getString(PLUGIN_TYPE).toLowerCase());
                logger.info("execute " + config.getString(PLUGIN_TYPE) + " output sql : {}", sql);
                break;
            default:
                break;
        }

    }

    @Override
    public void prepare(RuntimeEnvironment env) {

    }

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
        return new CheckResult(true, "");
    }

    private static List<String> parseSqlToList(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return null;
        }

        String[] values = sql.substring(sql.indexOf("("))
                .replaceAll("\\(","")
                .replaceAll("\\)","")
                .replaceAll("`","")
                .replaceAll("'","")
                .split("VALUES");

        return Arrays.asList(values);
    }
}
