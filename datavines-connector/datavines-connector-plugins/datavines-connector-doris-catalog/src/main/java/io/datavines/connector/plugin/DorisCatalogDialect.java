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

/**
 * Doris Catalog Dialect
 * 提供 Doris Catalog 特定的 SQL 方言支持
 * 支持 catalog.database.table 格式的完整表名
 */
public class DorisCatalogDialect extends DorisDialect {

    /**
     * 生成完整的表名限定符
     * 格式：catalog.database.table 或 database.table
     * 
     * @param catalogName catalog 名称（可选）
     * @param schemaName schema/database 名称
     * @param tableName 表名
     * @param withQuote 是否添加引号
     * @return 完整的表名
     */
    @Override
    public String getFullQualifiedTableName(String catalogName, String schemaName, String tableName, boolean withQuote) {
        StringBuilder fullTableName = new StringBuilder();
        
        // 如果有 catalog，添加 catalog.
        if (StringUtils.isNotEmpty(catalogName)) {
            if (withQuote) {
                fullTableName.append(quoteIdentifier(catalogName)).append(".");
            } else {
                fullTableName.append(catalogName).append(".");
            }
        }
        
        // 添加 database.
        if (StringUtils.isNotEmpty(schemaName)) {
            if (withQuote) {
                fullTableName.append(quoteIdentifier(schemaName)).append(".");
            } else {
                fullTableName.append(schemaName).append(".");
            }
        }
        
        // 添加 table
        if (withQuote) {
            fullTableName.append(quoteIdentifier(tableName));
        } else {
            fullTableName.append(tableName);
        }
        
        return fullTableName.toString();
    }

    /**
     * Doris Catalog 不支持错误数据输出到自身
     */
    @Override
    public boolean supportToBeErrorDataStorage() {
        return false;
    }

    /**
     * Doris Catalog 不支持错误数据输出到自身
     */
    @Override
    public boolean invalidateItemCanOutputToSelf() {
        return false;
    }
}
