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
package io.datavines.common.param.form;

import java.util.HashMap;
import java.util.Map;

/**
 * frontend form type
 */
public enum FormType {
    /**
     * 0-input
     * 1-radio
     * 2-select
     * 3-checkbox
     * 4-cascader
     * 5-textarea
     */
    INPUT(0,"input"),
    RADIO(1,"radio"),
    SELECT(2,"select"),
    SWITCH(3,"checkbox"),
    CASCADER(4,"cascader"),
    TEXTAREA(5,"textarea"),
    GROUP(6,"group");

    FormType(int code, String description) {
        this.code = code;
        this.description = description;
    }


    private final int code;

    private final String description;

    public int getCode() {
        return code;
    }


    public String getDescription() {
        return description;
    }

    private static final Map<Integer, FormType> VALUES_MAP = new HashMap<>();

    static {
        for (FormType type : FormType.values()) {
            VALUES_MAP.put(type.code,type);
        }
    }

    public static FormType of(Integer status) {
        if (VALUES_MAP.containsKey(status)) {
            return VALUES_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid code : " + status);
    }
}