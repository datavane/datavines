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

import com.baomidou.mybatisplus.annotation.TableField;
import io.datavines.common.exception.DataVinesException;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.Map;

@Data
public class MetricExecutionResult implements Serializable {

    private static final long serialVersionUID = -1L;

    private Double actualValue;

    private Double expectedValue;

    private String resultFormula;

    private String operator;

    private Double threshold;

    private String expectedType;

    private String metricName;

    private String metricDimension;

    private String metricType;

    private String databaseName;

    private String tableName;

    private String columnName;

    public MetricExecutionResult() {
    }

    public MetricExecutionResult(Map<String, Object> dataMap) {
        if (MapUtils.isEmpty(dataMap)) {
            throw new DataVinesException("data map is empty");
        }

        if (dataMap.get("actual_value") != null) {
            actualValue = Double.valueOf(String.valueOf(dataMap.get("actual_value")).trim());
        }

        if (dataMap.get("expected_value") != null) {
            expectedValue = Double.valueOf(String.valueOf(dataMap.get("expected_value")).trim());
        }

        if (dataMap.get("result_formula") != null) {
            resultFormula = String.valueOf(dataMap.get("result_formula")).trim();
        }

        if (dataMap.get("operator") != null) {
            operator = String.valueOf(dataMap.get("operator")).trim();
        }

        if (dataMap.get("threshold") != null) {
            threshold = Double.valueOf(String.valueOf(dataMap.get("threshold")).trim());
        }

        if (dataMap.get("metric_name") != null) {
            metricName = String.valueOf(dataMap.get("metric_name")).trim();
        }

        if (dataMap.get("metric_dimension") != null) {
            metricDimension = String.valueOf(dataMap.get("metric_dimension")).trim();
        }

        if (dataMap.get("metric_type") != null) {
            metricType = String.valueOf(dataMap.get("metric_type")).trim();
        }

        if (dataMap.get("database_name") != null) {
            databaseName = String.valueOf(dataMap.get("database_name")).trim();
        }

        if (dataMap.get("table_name") != null) {
            tableName = String.valueOf(dataMap.get("table_name")).trim();
        }

        if (dataMap.get("column_name") != null) {
            columnName = String.valueOf(dataMap.get("column_name")).trim();
        }

        if (dataMap.get("expected_type") != null) {
            expectedType = String.valueOf(dataMap.get("expected_type")).trim();
        }
    }
}