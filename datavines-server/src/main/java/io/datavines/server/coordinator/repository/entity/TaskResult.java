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
package io.datavines.server.coordinator.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("dv_task_result")
public class TaskResult implements Serializable {

    private static final long serialVersionUID = -1L;

    @TableId(type= IdType.AUTO)
    private Long id;

    @TableField(value = "task_id")
    private Long taskId;

    @TableField(value = "metric_name")
    private String metricName;

    @TableField(value = "metric_dimension")
    private String metricDimension;

    @TableField(value = "metric_type")
    private String metricType;

    @TableField(value = "database")
    private String database;

    @TableField(value = "table")
    private String table;

    @TableField(value = "column")
    private String column;

    @TableField(value = "actual_value")
    private Double actualValue;

    @TableField(value = "expected_value")
    private Double expectedValue;

    @TableField(value = "expected_type")
    private String expectedType;

    @TableField(value = "result_formula")
    private String resultFormula;

    @TableField(value = "operator")
    private String operator;

    @TableField(value = "threshold")
    private Double threshold;

    @TableField(value = "failure_strategy")
    private String failureStrategy;

    @TableField(value = "state")
    private String state;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
