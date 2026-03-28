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
package io.datavines.metric.api;

import io.datavines.common.enums.OperatorType;
import io.datavines.spi.PluginDiscovery;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

import static io.datavines.common.enums.OperatorType.EQ;

public class MetricValidator {

    /**
     * It is used to judge whether the result of the data quality task is failed
     * @return boolean
     */
    public static boolean isSuccess(MetricExecutionResult executionResult) {

        BigDecimal actualValue = executionResult.getActualValue();
        BigDecimal expectedValue = executionResult.getExpectedValue();

        OperatorType operatorType = OperatorType.of(StringUtils.trim(executionResult.getOperator()));
        ResultFormula resultFormula = PluginDiscovery.getMultiKeyPluginDiscovery(ResultFormula.class, ResultFormula::getPluginNames)
                .getOrCreatePlugin(executionResult.getResultFormula());

        return getCompareResult(operatorType,
                resultFormula.getResult(actualValue, expectedValue),
                executionResult.getThreshold());
    }

    public static BigDecimal getQualityScore(MetricExecutionResult executionResult, boolean isSuccess) {
        BigDecimal actualValue = executionResult.getActualValue();
        BigDecimal expectedValue = executionResult.getExpectedValue();

        ResultFormula resultFormula = PluginDiscovery.getMultiKeyPluginDiscovery(ResultFormula.class, ResultFormula::getPluginNames)
                .getOrCreatePlugin(executionResult.getResultFormula());
        SqlMetric metric = PluginDiscovery.getMultiKeyPluginDiscovery(SqlMetric.class, SqlMetric::getPluginNames)
                .getOrCreatePlugin(executionResult.getMetricName());

        return resultFormula.getScore(actualValue, expectedValue, isSuccess, metric.getDirectionType());
    }

    private static boolean getCompareResult(OperatorType operatorType, BigDecimal srcValue, BigDecimal targetValue) {
        if (srcValue == null || targetValue == null) {
            return false;
        }

        switch (operatorType) {
            case EQ:
                return srcValue.compareTo(targetValue) == 0;
            case LT:
                return srcValue.compareTo(targetValue) <= -1;
            case LTE:
                return srcValue.compareTo(targetValue) == 0 || srcValue.compareTo(targetValue) <= -1;
            case GT:
                return srcValue.compareTo(targetValue) >= 1;
            case GTE:
                return srcValue.compareTo(targetValue) == 0 || srcValue.compareTo(targetValue) >= 1;
            case NE:
                return srcValue.compareTo(targetValue) != 0;
            default:
                return true;
        }
    }
}
