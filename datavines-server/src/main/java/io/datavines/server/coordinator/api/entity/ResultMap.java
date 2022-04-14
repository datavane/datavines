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

package io.datavines.server.coordinator.api.entity;

import java.util.HashMap;

public class ResultMap extends HashMap<String, Object> {

    public static final String EMPTY = "";

    private int code;

    public ResultMap() {
    }

    public ResultMap success() {
        this.code = 200;
        this.put("code", this.code);
        this.put("msg", "Success");
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap fail() {
        this.code = 400;
        this.put("code", code);
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap fail(int code) {
        this.code = code;
        this.put("code", code);
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap message(String message) {
        this.put("msg", message);
        return this;
    }

    public ResultMap payload(Object object) {
        this.put("data", null == object ? EMPTY : object);
        return this;
    }

    public int getCode() {
        return code;
    }
}
