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

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import io.datavines.connector.api.StatementParser;
import io.datavines.connector.api.entity.StatementMetadataFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultStatementParser implements StatementParser {

    private final DbType dbType;

    public DefaultStatementParser() {
        this(null);
    }

    public DefaultStatementParser(DbType dbType) {
        this.dbType = dbType;
    }

    @Override
    public StatementMetadataFragment parseStatement(String statement) {
        if (statement == null || statement.trim().isEmpty()) {
            return null;
        }

        List<SQLStatement> stmtList = SQLUtils.parseStatements(statement, dbType);
        if (stmtList == null || stmtList.isEmpty()) {
            return null;
        }

        List<String> inputTables = new ArrayList<>();
        List<String> outputTables = new ArrayList<>();

        for (SQLStatement sqlStatement : stmtList) {
            SchemaStatVisitor visitor = SQLUtils.createSchemaStatVisitor(dbType);
            sqlStatement.accept(visitor);

            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            if (tables == null || tables.isEmpty()) {
                continue;
            }

            for (Map.Entry<TableStat.Name, TableStat> entry : tables.entrySet()) {
                String tableName = entry.getKey().getName();
                TableStat stat = entry.getValue();

                if (stat.getInsertCount() > 0 || stat.getCreateCount() > 0
                        || stat.getMergeCount() > 0) {
                    if (!outputTables.contains(tableName)) {
                        outputTables.add(tableName);
                    }
                }

                if (stat.getSelectCount() > 0) {
                    if (!inputTables.contains(tableName)) {
                        inputTables.add(tableName);
                    }
                }
            }
        }

        if (inputTables.isEmpty() && outputTables.isEmpty()) {
            return null;
        }

        return new StatementMetadataFragment(inputTables, outputTables, new ArrayList<>());
    }
}
