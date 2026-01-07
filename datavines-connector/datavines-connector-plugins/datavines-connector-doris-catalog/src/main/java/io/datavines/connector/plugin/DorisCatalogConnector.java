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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.datavines.common.ConfigConstants.CATALOG;

/**
 * Doris Catalog Connector
 * 核心连接器，负责与 Doris Catalog 交互
 * 关键特性：
 * 1. 每次获取连接后必须执行 SWITCH CATALOG
 * 2. 查询元数据使用 TABLE_CATALOG 过滤
 * 3. 验证 SWITCH CATALOG 执行成功
 */
public class DorisCatalogConnector extends DorisConnector {

    public DorisCatalogConnector(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new DorisCatalogDataSourceInfo(param);
    }

    /**
     * 获取连接并执行 SWITCH CATALOG
     * 这是核心方法，确保所有后续操作在正确的 Catalog 上下文中执行
     */
    @Override
    protected Connection getConnection(String dataSourceParam, Map<String,String> paramMap) throws SQLException {
        Connection connection = super.getConnection(dataSourceParam, paramMap);
        
        // 获取 catalog 名称并执行 SWITCH CATALOG
        String catalogName = paramMap.get(CATALOG);
        if (StringUtils.isNotEmpty(catalogName)) {
            switchCatalog(connection, catalogName);
        }
        
        return connection;
    }

    /**
     * 执行 SWITCH CATALOG 并验证
     * 
     * @param connection JDBC 连接
     * @param catalogName catalog 名称
     * @throws SQLException 如果切换失败或验证失败
     */
    private void switchCatalog(Connection connection, String catalogName) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            
            // 执行 SWITCH CATALOG
            String switchSql = "SWITCH " + catalogName;
            logger.info("Executing SWITCH CATALOG: {}", switchSql);
            stmt.execute(switchSql);
            
            // 验证切换成功
            rs = stmt.executeQuery("SELECT current_catalog()");
            if (rs.next()) {
                String currentCatalog = rs.getString(1);
                if (!catalogName.equals(currentCatalog)) {
                    throw new SQLException(String.format(
                        "SWITCH CATALOG failed: expected '%s' but got '%s'", 
                        catalogName, currentCatalog));
                }
                logger.info("Successfully switched to catalog: {}", currentCatalog);
            } else {
                throw new SQLException("Failed to verify current catalog");
            }
        } catch (SQLException e) {
            logger.error("Error switching to catalog {}: {}", catalogName, e.getMessage());
            throw new SQLException("Failed to switch to catalog " + catalogName + ": " + e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.warn("Error closing ResultSet", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.warn("Error closing Statement", e);
                }
            }
        }
    }

    /**
     * 获取表列表
     * 前置条件：已执行 SWITCH CATALOG
     */
    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is not valid");
        }

        Connection connection = getConnection(dataSourceParam, paramMap);

        List<TableInfo> tableList = new ArrayList<>();
        ResultSet tables = null;

        try {
            String schema = param.getDatabase();
            String catalog = paramMap.get(CATALOG);
            
            // 使用 catalog 和 schema 过滤
            tables = getMetadataTables(connection, catalog, schema);

            if (tables != null) {
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
            }

        } catch (Exception e) {
            logger.error("get table list error: ", e);
            throw new SQLException("Failed to get table list: " + e.getMessage(), e);
        } finally {
            JdbcDataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableList).build();
    }

    /**
     * 获取列信息
     * 前置条件：已执行 SWITCH CATALOG
     */
    @Override
    public ConnectorResponse getColumns(GetColumnsRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is not valid");
        }
        Connection connection = getConnection(dataSourceParam, paramMap);

        TableColumnInfo tableColumnInfo = null;
        try {
            String catalog = paramMap.get(CATALOG);
            String schema = param.getDataBase();
            String tableName = param.getTable();
            
            if (null != connection) {
                List<String> primaryKeys = new ArrayList<>();
                List<ColumnInfo> columns = getColumns(connection, catalog, schema, tableName);
                tableColumnInfo = new TableColumnInfo(tableName, primaryKeys, columns);
            }
        } catch (Exception e) {
            logger.error("get columns error: ", e);
            throw new SQLException("Failed to get columns: " + e.getMessage(), e);
        } finally {
            JdbcDataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableColumnInfo).build();
    }

    /**
     * 获取列信息（带 catalog 过滤）
     */
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
                // 大小写不敏感的情况下，可能会查询到不是当前表的字段
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

    /**
     * 查询元数据表（使用 catalog 过滤）
     */
    protected ResultSet getMetadataTables(Connection connection, String catalog, String schema) throws SQLException {
        Statement stmt = connection.createStatement();
        String sql = String.format(
            "SELECT TABLE_NAME, TABLE_TYPE, TABLE_COMMENT FROM information_schema.tables " +
            "WHERE TABLE_CATALOG = '%s' AND TABLE_SCHEMA = '%s'",
            catalog, schema
        );
        logger.debug("Executing metadata query: {}", sql);
        return stmt.executeQuery(sql);
    }

    /**
     * 查询元数据列（使用 catalog 过滤）
     */
    protected ResultSet getMetadataColumns(Connection connection, String catalog, String schema, String tableName, String columnName) throws SQLException {
        Statement stmt = connection.createStatement();
        String sql = String.format(
            "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.columns " +
            "WHERE TABLE_CATALOG = '%s' AND TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'",
            catalog, schema, tableName
        );
        logger.debug("Executing metadata query: {}", sql);
        return stmt.executeQuery(sql);
    }
}
