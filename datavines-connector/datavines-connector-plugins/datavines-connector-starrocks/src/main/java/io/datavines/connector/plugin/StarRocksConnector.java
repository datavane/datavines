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

import io.datavines.common.datasource.jdbc.BaseJdbcDataSourceInfo;
import io.datavines.common.datasource.jdbc.JdbcConnectionInfo;
import io.datavines.common.datasource.jdbc.entity.TableInfo;
import io.datavines.common.datasource.jdbc.utils.JdbcDataSourceUtils;
import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.GetTablesRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.connector.api.DataSourceClient;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StarRocksConnector extends MysqlConnector {

    public StarRocksConnector(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(JdbcConnectionInfo jdbcConnectionInfo) {
        return new StarRocksDataSourceInfo(jdbcConnectionInfo);
    }

    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        if (jdbcConnectionInfo == null) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        Connection connection = getConnection(dataSourceParam, jdbcConnectionInfo);

        List<TableInfo> tableList = null;
        ResultSet tables;

        try {
            String schema = param.getDatabase();
            tableList = new ArrayList<>();
            tables = getMetadataTables(connection, schema);

            if (null == tables) {
                return builder.result(tableList).build();
            }

            while (tables.next()) {
                String name = tables.getString(TABLE_NAME);
                if (!StringUtils.isEmpty(name)) {
                    String type = TABLE;
                    try {
                        type = tables.getString(TABLE_TYPE);
                    } catch (Exception e) {
                        // ignore
                    }
                    tableList.add(new TableInfo(schema, name, type, tables.getString("TABLE_COMMENT")));
                }
            }

        } catch (Exception e) {
            logger.error("get table list error: ", e);
        } finally {
            JdbcDataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableList).build();
    }

    protected ResultSet getMetadataTables(Connection connection, String schema) throws SQLException {
        java.sql.Statement stmt = connection.createStatement();
        return stmt.executeQuery("select TABLE_NAME, TABLE_TYPE, TABLE_COMMENT from information_schema.tables where TABLE_SCHEMA = '" + schema + "'");
    }
}
