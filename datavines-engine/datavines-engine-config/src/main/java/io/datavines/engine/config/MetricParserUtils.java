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

package io.datavines.engine.config;

import io.datavines.common.entity.TaskInfo;
import io.datavines.common.utils.Md5Utils;
import io.datavines.common.utils.StringUtils;

import org.apache.commons.collections4.MapUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.datavines.common.config.TransformConfig;
import io.datavines.common.entity.ExecuteSql;

import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.metric.api.SqlMetric;

public class MetricParserUtils {

    public static void operateInputParameter(Map<String, String> inputParameter,
                                             SqlMetric sqlMetric,
                                             TaskInfo task) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(ConfigConstants.YYYY_MM_DD_HH_MM_SS);
        LocalDateTime time = LocalDateTime.now();
        String now = df.format(time);

        inputParameter.put(ConfigConstants.METRIC_TYPE, StringUtils.wrapperSingleQuotes(sqlMetric.getType().getDescription()));
        inputParameter.put(ConfigConstants.METRIC_NAME, StringUtils.wrapperSingleQuotes(sqlMetric.getName()));
        inputParameter.put(ConfigConstants.METRIC_DIMENSION, StringUtils.wrapperSingleQuotes(sqlMetric.getDimension().getDescription()));
        inputParameter.put(ConfigConstants.CREATE_TIME, StringUtils.wrapperSingleQuotes(now));
        inputParameter.put(ConfigConstants.UPDATE_TIME, StringUtils.wrapperSingleQuotes(now));
        inputParameter.put(ConfigConstants.TASK_ID, String.valueOf(task.getId()));

        if (StringUtils.isEmpty(inputParameter.get(ConfigConstants.DATA_TIME))) {
            inputParameter.put(ConfigConstants.DATA_TIME, StringUtils.wrapperSingleQuotes(now));
        }

        if (StringUtils.isNotEmpty(inputParameter.get(ConfigConstants.REGEXP_PATTERN))) {
            inputParameter.put(ConfigConstants.REGEXP_PATTERN, StringUtils.escapeJava(
                    StringUtils.escapeJava(inputParameter.get(ConfigConstants.REGEXP_PATTERN))));
        }

        sqlMetric.prepare(inputParameter);
    }

    public static void setTransformerConfig(Map<String, String> inputParameterValueResult,
                                            List<TransformConfig> transformerConfigList,
                                            List<ExecuteSql> executeSqlList) {
        int index = 0;
        for (ExecuteSql executeSql: executeSqlList) {
            if (StringUtils.isEmpty(executeSql.getSql())
                    || StringUtils.isEmpty(executeSql.getResultTable())) {
                continue;
            }
            Map<String,Object> config = new HashMap<>();
            config.put(ConfigConstants.INDEX,index++);
            config.put(ConfigConstants.SQL, PlaceholderUtils.replacePlaceholders(executeSql.getSql(), inputParameterValueResult, true));
            config.put(ConfigConstants.OUTPUT_TABLE,executeSql.getResultTable());

            TransformConfig transformerConfig = new TransformConfig(ConfigConstants.SQL,config);
            transformerConfigList.add(transformerConfig);
        }
    }

    public static void setTransformerConfig(Map<String, String> inputParameterValueResult,
                                            List<TransformConfig> transformerConfigList,
                                            ExecuteSql executeSql,
                                            String type) {
        int index = 0;

        if (StringUtils.isEmpty(executeSql.getSql())
                || StringUtils.isEmpty(executeSql.getResultTable())) {
            return;
        }

        Map<String,Object> config = new HashMap<>();
        config.put(ConfigConstants.INDEX, index++);
        config.put(ConfigConstants.SQL, PlaceholderUtils.replacePlaceholders(executeSql.getSql(), inputParameterValueResult,true));
        config.put(ConfigConstants.OUTPUT_TABLE, executeSql.getResultTable());
        config.put(ConfigConstants.INVALIDATE_ITEMS_TABLE, inputParameterValueResult.get(ConfigConstants.INVALIDATE_ITEMS_TABLE));

        TransformConfig transformerConfig = new TransformConfig(ConfigConstants.SQL, config);
        transformerConfig.setType(type);
        transformerConfigList.add(transformerConfig);
    }

    /**
     * the unique code use to get the same type and condition task statistics value
     * @param inputParameterValue
     * @return
     */
    public static String generateUniqueCode(Map<String, String> inputParameterValue) {

        if (MapUtils.isEmpty(inputParameterValue)) {
            return "-1";
        }

        Map<String,String> newInputParameterValue = new HashMap<>(inputParameterValue);

        newInputParameterValue.remove(ConfigConstants.METRIC_TYPE);
        newInputParameterValue.remove(ConfigConstants.METRIC_NAME);
        newInputParameterValue.remove(ConfigConstants.CREATE_TIME);
        newInputParameterValue.remove(ConfigConstants.UPDATE_TIME);
        newInputParameterValue.remove(ConfigConstants.TASK_ID);
        newInputParameterValue.remove(ConfigConstants.RESULT_FORMULA);
        newInputParameterValue.remove(ConfigConstants.OPERATOR);
        newInputParameterValue.remove(ConfigConstants.THRESHOLD);
        newInputParameterValue.remove(ConfigConstants.FAILURE_STRATEGY);
        newInputParameterValue.remove(ConfigConstants.DATA_TIME);
        newInputParameterValue.remove(ConfigConstants.ERROR_OUTPUT_PATH);
        newInputParameterValue.remove(ConfigConstants.EXPECTED_TYPE);
        newInputParameterValue.remove(ConfigConstants.EXPECTED_NAME);
        newInputParameterValue.remove(ConfigConstants.EXPECTED_TABLE);

        StringBuilder sb = new StringBuilder();
        for (String value : newInputParameterValue.values()) {
            sb.append(value);
        }

        return Md5Utils.getMd5(sb.toString(),true);
    }
}
