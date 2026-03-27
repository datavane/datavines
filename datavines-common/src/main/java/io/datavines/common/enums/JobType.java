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
package io.datavines.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.HashMap;

/**
 * job type
 */
public enum JobType {
    /**
     * 0 DATA_QUALITY
     * 1 DATA_PROFILE
     * 2 DATA_RECONCILIATION
     */
    DATA_QUALITY(0, "DATA_QUALITY", "数据质量检查"),
    DATA_PROFILE(1, "DATA_PROFILE","数据概览检查"),
    DATA_RECONCILIATION(2, "DATA_RECONCILIATION","数据比对检查");

    JobType(int code, String description, String zhDescription) {
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

    private static final HashMap<String, JobType> JOB_TYPE_MAP = new HashMap<>();

    static {
        for (JobType jobType: JobType.values()){
            JOB_TYPE_MAP.put(jobType.description, jobType);
        }
    }

    public static JobType of(String jobType){
        if(JOB_TYPE_MAP.containsKey(jobType)){
            return JOB_TYPE_MAP.get(jobType);
        }
        throw new IllegalArgumentException("invalid job type : " + jobType);
    }

    public static JobType of(int code){
        for (JobType jobType : JobType.values()) {
            if (code == jobType.code) {
                return jobType;
            }
        }
        throw new IllegalArgumentException("invalid job type code: " + code);
    }
}
