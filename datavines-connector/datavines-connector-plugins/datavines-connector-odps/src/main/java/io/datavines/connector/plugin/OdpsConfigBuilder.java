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
package io.datavines.connector.plugin;

import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.type.InputParam;

public class OdpsConfigBuilder extends JdbcConfigBuilder {

    @Override
    protected InputParam getPropertiesInput(boolean isEn) {
        return getInputParam("properties",
                isEn ? "properties" : "参数",
                isEn ? "please enter properties,like key=value&key1=value1" : "请填入参数，格式为key=value&key1=value1", 2, null,
                "interactiveMode=true&enableCommandApi=true");
    }
@Override
    protected InputParam getPortInput(boolean isEn) {
        return getInputParam("port",
                isEn ? "port" : "端口",
                isEn ? "please enter port" : "请填入端口号", 1,
                Validate.newBuilder().setRequired(false).setMessage(isEn ? "please enter port" : "请填入端口号").build(),
                null);
    }
    protected InputParam getUserInput(boolean isEn) {
        return getInputParam("user",
                isEn ? "access_id" : "access_id",
                isEn ? "please enter access_id" : "请填入access_id", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter access_id" : "请填入access_id").build(),
                null);
    }

    protected InputParam getPasswordInput(boolean isEn) {
        return getInputParam("password",
                isEn ? "access_key" : "access_key",
                isEn ? "please enter access_key" : "请填入access_key", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter access_key" : "请填入access_key").build(),
                null);
    }
}
