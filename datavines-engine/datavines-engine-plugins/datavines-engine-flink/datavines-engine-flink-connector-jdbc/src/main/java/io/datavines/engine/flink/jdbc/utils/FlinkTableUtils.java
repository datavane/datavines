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
package io.datavines.engine.flink.jdbc.utils;

import io.datavines.common.config.Config;
import io.datavines.common.utils.StringUtils;
import io.datavines.engine.common.utils.ParserUtils;
import io.datavines.engine.flink.api.entity.FLinkColumnInfo;

import java.util.List;

public class FlinkTableUtils {

    public static String generateCreateTableStatement(String database, String outputTable, String tableName, List<FLinkColumnInfo> columns, Config config) {
        StringBuilder createTableSql = new StringBuilder();
        createTableSql.append("CREATE TABLE ").append("`").append(outputTable).append("`").append(" (\n");

        for (FLinkColumnInfo column : columns) {
            createTableSql.append("  `").append(column.getColumn()).append("` ").append(mapDataType(column.getDataType())).append(",\n");
        }

        String url = config.getString("url");
        url = DatabaseUrlReplacer.replaceDatabase(url, database);

        createTableSql.setLength(createTableSql.length() - 2);
        createTableSql.append("\n) WITH (\n");
        createTableSql.append("  'connector' = 'jdbc',\n");
        createTableSql.append("  'url' = '").append(url).append("',\n");
        createTableSql.append("  'table-name' = '").append(tableName).append("',\n");
        if (!StringUtils.isEmptyOrNullStr(config.getString("password"))) {
            createTableSql.append("  'password' = '").append(ParserUtils.decode(config.getString("password"))).append("',\n");
        }
        createTableSql.append("  'username' = '").append(config.getString("user")).append("'\n");
        createTableSql.append(")");

        return createTableSql.toString();
    }

    public static String generateCreateTableStatement(String database, String outputTable, List<FLinkColumnInfo> columns) {
        StringBuilder createTableSql = new StringBuilder();
        createTableSql.append("CREATE TABLE ").append(database).append(".").append(outputTable).append(" (\n");

        for (FLinkColumnInfo column : columns) {
            createTableSql.append("  `").append(column.getColumn()).append("` ").append(mapDataTypeToSource(column.getDataType())).append(",\n");
        }

        // Remove the last comma and newline
        createTableSql.setLength(createTableSql.length() - 2);
        createTableSql.append("\n)");

        return createTableSql.toString();
    }

    private static String mapDataType(String flinkType) {
        switch (flinkType.toUpperCase().split(" ")[0].split("\\(")[0]) {
            case "VARCHAR":
            case "TEXT":
            case "CHAR":
                return "STRING";
            case "INT":
            case "INTEGER":
            case "SMALLINT":
            case "TINYINT":
                return "INT";
            case "BIGINT":
                return "BIGINT";
            case "DOUBLE":
            case "FLOAT":
            case "REAL":
                return "DOUBLE";
            case "DECIMAL":
            case "NUMERIC":
                return "DECIMAL";
            case "DATE":
                return "DATE";
            case "TIME":
                return "TIME";
            case "DATETIME":
            case "TIMESTAMP":
                return "TIMESTAMP";
            case "BOOLEAN":
            case "BIT":
                return "BOOLEAN";
            case "BINARY":
            case "VARBINARY":
            case "LONGVARBINARY":
                return "BYTES";
            default:
                return "STRING";
        }
    }

    private static String mapDataTypeToSource(String flinkType) {
        switch (flinkType.toUpperCase().split(" ")[0].split("\\(")[0]) {
            case "STRING":
                return "TEXT";
            case "INT":
            case "INTEGER":
            case "SMALLINT":
            case "TINYINT":
                return "INT";
            case "BIGINT":
                return "BIGINT";
            case "DOUBLE":
            case "FLOAT":
            case "REAL":
                return "DOUBLE";
            case "DECIMAL":
            case "NUMERIC":
                return "DECIMAL";
            case "DATE":
                return "DATE";
            case "TIME":
                return "TIME";
            case "DATETIME":
            case "TIMESTAMP":
                return "DATETIME";
            case "BOOLEAN":
            case "BIT":
                return "BOOLEAN";
            case "BINARY":
            case "VARBINARY":
            case "LONGVARBINARY":
                return "BYTES";
            default:
                return "TEXT";
        }
    }
}
