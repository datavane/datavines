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
package io.datavines.engine.jdbc.config;

import io.datavines.common.config.SinkConfig;
import io.datavines.common.config.enums.SinkType;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.datavines.engine.config.ConfigConstants.UNIQUE_CODE;
import static io.datavines.engine.config.MetricParserUtils.generateUniqueCode;

public class JdbcSingleTableMetricBuilder extends BaseJdbcConfigurationBuilder {

    @Override
    public void buildSinkConfigs() throws DataVinesException {

        inputParameter.put(UNIQUE_CODE, StringUtils.wrapperSingleQuotes(generateUniqueCode(inputParameter)));
        List<SinkConfig> sinkConfigs = new ArrayList<>();
        //get the actual value storage parameter
        SinkConfig actualValueSinkConfig = getDefaultSinkConfig(SinkSqlBuilder.getActualValueSql(), "dv_actual_values");
        actualValueSinkConfig.setType(SinkType.ACTUAL_VALUE.getDescription());
        sinkConfigs.add(actualValueSinkConfig);

        //get the task data storage parameter
        SinkConfig taskResultSinkConfig = getDefaultSinkConfig(SinkSqlBuilder.getTaskResultSql(),  "dv_task_result");
        taskResultSinkConfig.setType(SinkType.TASK_RESULT.getDescription());
        sinkConfigs.add(taskResultSinkConfig);

        //get the error data storage parameter
        //support file(hdfs/minio/s3)/es

        configuration.setSinkParameters(sinkConfigs);
    }

}
