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

public class ColumnInfo {

    private String name;
    private String parameterName;
    private boolean needSingleQuotation;

    public ColumnInfo(String name, boolean needSingleQuotation) {
        this(name,needSingleQuotation,name);
    }

    public ColumnInfo(String name, boolean needSingleQuotation,String parameterName) {
        this.name = name;
        this.needSingleQuotation = needSingleQuotation;
        this.parameterName = parameterName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public boolean isNeedSingleQuotation() {
        return needSingleQuotation;
    }

    public void setNeedSingleQuotation(boolean needSingleQuotation) {
        this.needSingleQuotation = needSingleQuotation;
    }
}
