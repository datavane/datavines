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

import io.datavines.common.utils.Md5Utils;
import io.datavines.common.utils.StringUtils;

import java.util.Map;

import static io.datavines.common.ConfigConstants.*;

/**
 * Doris Catalog Parameter Converter
 * 参数转换器，负责参数转换、验证和 UUID 生成
 * 关键特性：强制验证 catalog 参数必填
 */
public class DorisCatalogParameterConverter extends DorisParameterConverter {

    /**
     * 参数转换
     * 验证 catalog 参数必填，并进行参数转换
     */
    @Override
    public Map<String, Object> converter(Map<String, Object> parameter) {
        // 验证 catalog 参数
        validateCatalogParameter(parameter);
        
        // 调用父类的转换逻辑
        return super.converter(parameter);
    }

    /**
     * 验证 catalog 参数是否存在且不为空
     */
    private void validateCatalogParameter(Map<String, Object> parameter) {
        Object catalog = parameter.get(CATALOG);
        if (catalog == null || StringUtils.isEmpty(catalog.toString())) {
            throw new IllegalArgumentException("catalog 参数为必填项，请提供有效的 Catalog 名称");
        }
    }

    /**
     * 生成连接器 UUID
     * UUID 用于标识唯一的数据源连接
     * 组成：host@port@catalog@database
     */
    @Override
    public String getConnectorUUID(Map<String, Object> parameter) {
        StringBuilder sb = new StringBuilder();
        
        // 添加 host
        if (parameter.get(HOST) != null) {
            sb.append(parameter.get(HOST).toString());
        }
        sb.append("@");
        
        // 添加 port
        if (parameter.get(PORT) != null) {
            sb.append(parameter.get(PORT).toString());
        }
        sb.append("@");
        
        // 添加 catalog（必须包含）
        if (parameter.get(CATALOG) != null) {
            sb.append(parameter.get(CATALOG).toString());
        }
        sb.append("@");
        
        // 添加 database（可选）
        if (parameter.get(DATABASE) != null) {
            sb.append(parameter.get(DATABASE).toString());
        }
        
        // 计算 MD5
        return Md5Utils.getMd5(sb.toString(), false);
    }
}
