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

import java.math.BigDecimal;

public enum DataQualityLevel {

    UNQUALIFIED(1, "unqualified","不合格"),
    QUALIFIED(2, "qualified","合格"),
    MEDIUM(3, "medium","中等"),
    GOOD(4, "good","良好"),
    EXCELLENT(5, "excellent","优秀");

    DataQualityLevel(int code, String description, String zhDescription) {
        this.code = code;
        this.description = description;
        this.zhDescription = zhDescription;
    }

    @EnumValue
    private final int code;

    private final String description;

    private final String zhDescription;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getZhDescription() {
        return zhDescription;
    }

    public static DataQualityLevel getQualityLevelByScore(BigDecimal score) {
        if ((score.compareTo(new BigDecimal(100)) == 0 || score.compareTo(new BigDecimal(100)) <= -1)
            && (score.compareTo(new BigDecimal(80)) >= 0)) {
            return EXCELLENT;
        } else if ((score.compareTo(new BigDecimal(80)) == 0 || score.compareTo(new BigDecimal(80)) <= -1)
                && (score.compareTo(new BigDecimal(60)) >= 0)) {
            return GOOD;
        } else if ((score.compareTo(new BigDecimal(60)) == 0 || score.compareTo(new BigDecimal(60)) <= -1)
                && (score.compareTo(new BigDecimal(40)) >= 0)) {
            return MEDIUM;
        } else if ((score.compareTo(new BigDecimal(40)) == 0 || score.compareTo(new BigDecimal(40)) <= -1)
                && (score.compareTo(new BigDecimal(20)) >= 0)) {
            return QUALIFIED;
        } else {
            return UNQUALIFIED;
        }
    }
}
