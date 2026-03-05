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
import io.datavines.common.CommonConstants;
import io.datavines.common.param.form.PluginParams;
import io.datavines.common.param.form.PropsType;
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.props.InputParamsProps;
import io.datavines.common.param.form.type.InputParam;
import io.datavines.connector.api.ConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * MaxCompute config builder for UI form generation
 */
public class MaxComputeConfigBuilder implements ConfigBuilder {

    private final Logger logger = LoggerFactory.getLogger(MaxComputeConfigBuilder.class);

    @Override
    public String build(boolean isEn) {
        List<PluginParams> params = new ArrayList<>();
        params.add(getEndpointInput(isEn));
        params.add(getProjectInput(isEn));
        params.add(getAccessKeyIdInput(isEn));
        params.add(getAccessKeySecretInput(isEn));
        params.add(getPropertiesInput(isEn));

        return toJsonString(params);
    }

    @Override
    public String buildErrorDataStorage(boolean isEn) {
        List<PluginParams> params = new ArrayList<>();
        params.add(getEndpointInput(isEn));
        params.add(getProjectInput(isEn));
        params.add(getAccessKeyIdInput(isEn));
        params.add(getAccessKeySecretInput(isEn));
        params.add(getPropertiesInput(isEn));

        return toJsonString(params);
    }

    private InputParam getEndpointInput(boolean isEn) {
        return InputParam
                .newBuilder("endpoint", isEn ? "Endpoint" : "服务地址")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .setMessage(isEn ? "please enter MaxCompute endpoint" : "请填入 MaxCompute 服务地址")
                        .build())
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXT)
                .setRows(1)
                .setPlaceholder(isEn
                        ? "please enter MaxCompute endpoint, e.g. http://service.cn-hangzhou.maxcompute.aliyun.com/api"
                        : "请填入 MaxCompute 服务地址，如 http://service.cn-hangzhou.maxcompute.aliyun.com/api")
                .setValue(null)
                .setEmit(null)
                .build();
    }

    private InputParam getProjectInput(boolean isEn) {
        return InputParam
                .newBuilder("database", isEn ? "Project" : "项目")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .setMessage(isEn ? "please enter project name" : "请填入项目名称")
                        .build())
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXT)
                .setRows(1)
                .setPlaceholder(isEn ? "please enter project name" : "请填入项目名称")
                .setValue(null)
                .setEmit(null)
                .build();
    }

    private InputParam getAccessKeyIdInput(boolean isEn) {
        return InputParam
                .newBuilder("user", isEn ? "AccessKey ID" : "AccessKey ID")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .setMessage(isEn ? "please enter AccessKey ID" : "请填入 AccessKey ID")
                        .build())
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXT)
                .setRows(1)
                .setPlaceholder(isEn ? "please enter AccessKey ID" : "请填入 AccessKey ID")
                .setValue(null)
                .setEmit(null)
                .build();
    }

    private InputParam getAccessKeySecretInput(boolean isEn) {
        return InputParam
                .newBuilder("password", isEn ? "AccessKey Secret" : "AccessKey Secret")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .setMessage(isEn ? "please enter AccessKey Secret" : "请填入 AccessKey Secret")
                        .build())
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.PASSWORD)
                .setRows(1)
                .setPlaceholder(isEn ? "please enter AccessKey Secret" : "请填入 AccessKey Secret")
                .setValue(null)
                .setEmit(null)
                .build();
    }

    private InputParam getPropertiesInput(boolean isEn) {
        return InputParam
                .newBuilder("properties", isEn ? "Properties" : "参数")
                .addValidate(null)
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXTAREA)
                .setRows(2)
                .setPlaceholder(isEn
                        ? "please enter properties, like key=value&key1=value1"
                        : "请填入参数，格式为key=value&key1=value1")
                .setValue(null)
                .setEmit(null)
                .build();
    }

    private String toJsonString(List<PluginParams> params) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            logger.error("json parse error : ", e);
            return "[]";
        }
    }
}
