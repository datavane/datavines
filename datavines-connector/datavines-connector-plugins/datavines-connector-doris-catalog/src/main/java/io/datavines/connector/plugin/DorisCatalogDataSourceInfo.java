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

import io.datavines.common.utils.StringUtils;

import java.util.Map;

/**
 * Doris Catalog Data Source Info
 * 负责构建 Doris Catalog 专用的 JDBC 连接 URL，支持 catalog.database 格式
 * 强制要求 catalog 参数为必填项
 */
public class DorisCatalogDataSourceInfo extends DorisDataSourceInfo {

    public DorisCatalogDataSourceInfo(Map<String,String> param) {
        super(param);
        // 验证 catalog 参数必填
        validateCatalog();
    }

    /**
     * 验证 catalog 参数是否存在
     * catalog 是 Doris Catalog Connector 的必填项
     */
    private void validateCatalog() {
        String catalog = getCatalog();
        if (StringUtils.isEmpty(catalog)) {
            throw new IllegalArgumentException("catalog 参数为必填项，请提供有效的 Catalog 名称");
        }
    }

    /**
     * 构建 JDBC URL
     * 格式：
     * 1. 有 database: jdbc:mysql://host:port/catalog.database
     * 2. 无 database: jdbc:mysql://host:port/catalog
     */
    @Override
    public String getJdbcUrl() {
        StringBuilder jdbcUrl = new StringBuilder(getAddress());
        appendCatalogAndDatabase(jdbcUrl);
        appendProperties(jdbcUrl);
        return jdbcUrl.toString();
    }

    /**
     * 追加 Catalog 和 Database 到 JDBC URL
     * catalog 是必填的，database 是可选的
     */
    private void appendCatalogAndDatabase(StringBuilder jdbcUrl) {
        String catalog = getCatalog();
        String database = getDatabase();
        
        // 确保有 '/' 分隔符
        if (jdbcUrl.charAt(jdbcUrl.length() - 1) != '/') {
            jdbcUrl.append("/");
        }
        
        // 追加 catalog
        jdbcUrl.append(catalog);
        
        // 如果有 database，追加 .database
        if (StringUtils.isNotEmpty(database)) {
            jdbcUrl.append(".").append(database);
        }
    }

    @Override
    public String getType() {
        return "doris_catalog";
    }
}
