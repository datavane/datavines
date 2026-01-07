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
import lombok.extern.slf4j.Slf4j;

/**
 * Doris Catalog Config Builder
 * 前端配置构建器，定义用户输入的参数项及验证规则
 * 关键特性：catalog 参数为必填项
 */
@Slf4j
public class DorisCatalogConfigBuilder extends DorisConfigBuilder {

    /**
     * Catalog 参数输入配置（必填）
     * 这是 Doris Catalog Connector 的核心配置项
     */
    @Override
    protected InputParam getCatalogInput(boolean isEn) {
        return getInputParam("catalog",
                isEn ? "Catalog" : "Catalog 名称",
                isEn ? "Please enter Catalog name (Required)" : "请输入 Catalog 名称（必填）", 
                1,
                Validate.newBuilder()
                        .setRequired(true)
                        .setMessage(isEn ? "Catalog is required" : "Catalog 为必填项")
                        .build(),
                null);
    }

    /**
     * Database 参数输入配置（可选）
     * 对于 Doris Catalog，database 是可选的
     */
    @Override
    protected InputParam getDatabaseInput(boolean isEn) {
        return getInputParam("database",
                isEn ? "Database" : "数据库",
                isEn ? "Please enter database name (Optional)" : "请输入数据库名称（可选）", 
                1, 
                Validate.newBuilder()
                        .setRequired(false)
                        .build(),
                null);
    }

    /**
     * 连接参数配置
     */
    @Override
    protected InputParam getPropertiesInput(boolean isEn) {
        return getInputParam("properties",
                isEn ? "Properties" : "连接参数",
                isEn ? "Please enter properties, format: key=value&key1=value1" : "请输入连接参数，格式：key=value&key1=value1", 
                2, 
                null,
                "useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&useInformationSchema=true");
    }

    /**
     * 密码参数配置
     */
    @Override
    protected InputParam getPasswordInput(boolean isEn) {
        return getInputParam("password",
                isEn ? "Password" : "密码",
                isEn ? "Please enter password" : "请输入密码", 
                1, 
                Validate.newBuilder()
                        .setRequired(false)
                        .build(),
                null);
    }
}
