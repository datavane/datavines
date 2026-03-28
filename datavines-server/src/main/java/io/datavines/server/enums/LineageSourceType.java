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
package io.datavines.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.HashMap;
import java.util.Map;

public enum LineageSourceType {

    /**
     * 0-data quality task
     * 1-catalog task
     */
    MANUAL(0, "manual"),
    SQL_PARSER(1, "sql_parser"),
    SPARK_LISTENER(2, "spark_listener"),
    FLINK_SQL_LINEAGE(3, "flink_sql_lineage"),
    ;
    LineageSourceType(int code, String description){
        this.code = code;
        this.description = description;
    }

    @EnumValue
    private final int code;

    private final String description;

    private static final Map<Integer, LineageSourceType> CODE_2_MAP = new HashMap<>();

    private static final Map<String, LineageSourceType> DESC_2_MAP = new HashMap<>();

    static {
        for (LineageSourceType commandCategory : LineageSourceType.values()) {
            CODE_2_MAP.put(commandCategory.code,commandCategory);
            DESC_2_MAP.put(commandCategory.description,commandCategory);
        }
    }

    public static LineageSourceType of(Integer status) {
        if (CODE_2_MAP.containsKey(status)) {
            return CODE_2_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid lineage source type : " + status);
    }

    public static LineageSourceType descOf(String description) {
        if (DESC_2_MAP.containsKey(description)) {
            return DESC_2_MAP.get(description);
        }
        throw new IllegalArgumentException("invalid lineage source type : " + description);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
