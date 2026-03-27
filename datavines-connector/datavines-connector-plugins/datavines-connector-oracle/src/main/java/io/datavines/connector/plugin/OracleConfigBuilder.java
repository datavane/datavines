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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datavines.common.param.form.PluginParams;
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.type.InputParam;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OracleConfigBuilder extends JdbcConfigBuilder {

    @Override
    public String build(boolean isEn) {
        List<PluginParams> params = new ArrayList<>();
        params.add(getHostInput(isEn));
        params.add(getPortInput(isEn));
        params.add(getSID(isEn));
        params.add(getUserInput(isEn));
        params.add(getPasswordInput(isEn));
        params.add(getPropertiesInput(isEn));
        params.addAll(getOtherParams(isEn));
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("json parse error : ", e);
        }

        return result;
    }

    private InputParam getSID(boolean isEn) {
        return getInputParam("sid",
                isEn ? "sid" : "SID",
                isEn ? "please enter sid" : "请填入SID", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter sid" : "请填入SID").build(), null);
    }

}
