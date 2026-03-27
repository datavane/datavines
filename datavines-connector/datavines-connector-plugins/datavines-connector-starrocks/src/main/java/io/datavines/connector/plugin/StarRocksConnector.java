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
import io.datavines.common.datasource.jdbc.entity.ColumnInfo;
import io.datavines.common.datasource.jdbc.entity.TableColumnInfo;
import io.datavines.common.datasource.jdbc.entity.TableInfo;
import io.datavines.common.datasource.jdbc.utils.JdbcDataSourceUtils;
import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.GetColumnsRequestParam;
import io.datavines.common.param.GetTablesRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.DataSourceClient;
import org.apache.commons.collections4.MapUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.datavines.common.ConfigConstants.CATALOG;

public class StarRocksConnector extends MysqlConnector {

    public StarRocksConnector(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new StarRocksDataSourceInfo(param);
    }

    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        Connection connection = getConnection(dataSourceParam, paramMap);

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

    @Override
    public ConnectorResponse getColumns(GetColumnsRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        Connection connection = getConnection(dataSourceParam, paramMap);

        TableColumnInfo tableColumnInfo = null;
        try {
            String catalog;
            String schema;

            if (StringUtils.isNotEmpty(paramMap.get(CATALOG))) {
                catalog = paramMap.get(CATALOG);
                schema = param.getDataBase();
            } else {
                catalog = null;
                schema = param.getDataBase();
            }

            String tableName = param.getTable();
            if (null != connection) {
                List<String> primaryKeys = new ArrayList<>();
                List<ColumnInfo> columns = getColumns(connection, catalog, schema, tableName);
                tableColumnInfo = new TableColumnInfo(tableName, primaryKeys, columns);
            }
        } finally {
            JdbcDataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableColumnInfo).build();
    }

    public List<ColumnInfo> getColumns(Connection connection, String catalog, String schema, String tableName) {
        ResultSet rs = null;
        List<ColumnInfo> columnList = new ArrayList<>();
        try {
            rs = getMetadataColumns(connection, catalog, schema, tableName, "%");
            if (rs == null) {
                return columnList;
            }
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String rawType = rs.getString("COLUMN_TYPE");
                String comment = rs.getString("COLUMN_COMMENT");
                String curTableName = rs.getString("TABLE_NAME");
                // If the meta database is case-insensitive, it will identify fields that are not in the current table.
                // e.g. When querying a table named test, both the test and TEST table fields will be queried simultaneously.
                if(tableName.equals(curTableName)){
                    columnList.add(new ColumnInfo(name, rawType, comment,false));
                }
            }
        } catch (Exception e) {
            logger.error("get column error, param is {} :", schema + "." + tableName, e);
        } finally {
            JdbcDataSourceUtils.closeResult(rs);
        }
        return columnList;
    }

    protected ResultSet getMetadataTables(Connection connection, String schema) throws SQLException {
        java.sql.Statement stmt = connection.createStatement();
        return stmt.executeQuery("select TABLE_NAME, TABLE_TYPE, TABLE_COMMENT from information_schema.tables where TABLE_SCHEMA = '" + schema + "'");
    }

    protected ResultSet getMetadataColumns(Connection connection, String catalog, String schema, String tableName, String columnName) throws SQLException {
        java.sql.Statement stmt = connection.createStatement();
        return stmt.executeQuery("select TABLE_NAME, COLUMN_NAME, COLUMN_TYPE ,COLUMN_COMMENT from information_schema.columns where TABLE_SCHEMA = '" + schema + "' AND TABLE_NAME ='" + tableName + "'");
    }
}
