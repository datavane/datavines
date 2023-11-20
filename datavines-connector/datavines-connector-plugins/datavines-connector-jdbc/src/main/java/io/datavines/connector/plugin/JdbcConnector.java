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

import io.datavines.common.datasource.jdbc.*;
import io.datavines.common.datasource.jdbc.entity.ColumnInfo;
import io.datavines.common.datasource.jdbc.entity.DatabaseInfo;
import io.datavines.common.datasource.jdbc.entity.TableInfo;
import io.datavines.common.datasource.jdbc.entity.TableColumnInfo;
import io.datavines.common.param.*;
import io.datavines.common.utils.JSONUtils;
import io.datavines.connector.api.Connector;
import io.datavines.common.datasource.jdbc.utils.JdbcDataSourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.datavines.common.ConfigConstants.HOST;
import static io.datavines.common.ConfigConstants.PORT;

public abstract class JdbcConnector implements Connector, IJdbcDataSourceInfo {

    protected final Logger logger = LoggerFactory.getLogger(JdbcConnector.class);

    protected static final String TABLE = "TABLE";

    protected static final String DATABASE = "DATABASE";

    protected static final String VIEW = "VIEW";

    protected static final String[] TABLE_TYPES = new String[]{TABLE, VIEW};

    protected static final String TABLE_NAME = "TABLE_NAME";

    protected static final String TABLE_TYPE = "TABLE_TYPE";

    private final JdbcExecutorClientManager jdbcExecutorClientManager = JdbcExecutorClientManager.getInstance();

    @Override
    public ConnectorResponse getDatabases(GetDatabasesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        JdbcExecutorClient executorClient = getJdbcExecutorClient(dataSourceParam);
        Connection connection = executorClient.getConnection();
        List<DatabaseInfo> databaseList = new ArrayList<>();
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        String curDatabase = jdbcConnectionInfo == null ?
                "" : StringUtils.isEmpty(jdbcConnectionInfo.getCatalog()) ?
                        jdbcConnectionInfo.getDatabase() : jdbcConnectionInfo.getCatalog();
        if (StringUtils.isEmpty(curDatabase)) {
            ResultSet rs = getMetadataDatabases(connection);
            while (rs.next()) {
                databaseList.add(new DatabaseInfo(rs.getString(1), DATABASE));
            }
        }else {
            databaseList.add(new DatabaseInfo(curDatabase, DATABASE));
        }
        JdbcDataSourceUtils.releaseConnection(connection);
        builder.result(databaseList);
        return builder.build();
    }

    private JdbcExecutorClient getJdbcExecutorClient(String dataSourceParam) {
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        return jdbcExecutorClientManager.getExecutorClient(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(jdbcConnectionInfo)));
    }

    private JdbcExecutorClient getJdbcExecutorClient(String dataSourceParam, JdbcConnectionInfo jdbcConnectionInfo) {
        return jdbcExecutorClientManager.getExecutorClient(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(jdbcConnectionInfo)));
    }

    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        if (jdbcConnectionInfo == null) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        JdbcExecutorClient executorClient = getJdbcExecutorClient(dataSourceParam, jdbcConnectionInfo);
        Connection connection = executorClient.getConnection();

        List<TableInfo> tableList = null;
        ResultSet tables = null;

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = StringUtils.isEmpty(jdbcConnectionInfo.getSchema()) ? jdbcConnectionInfo.getCatalog() : param.getDataBase();
            String schema = StringUtils.isEmpty(jdbcConnectionInfo.getSchema()) ?  param.getDataBase() : jdbcConnectionInfo.getSchema();

            tableList = new ArrayList<>();
            tables = getMetadataTables(metaData, catalog, schema);

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
                    tableList.add(new TableInfo(schema, name, type, tables.getString("REMARKS")));
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
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        if (jdbcConnectionInfo == null) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        JdbcExecutorClient executorClient = getJdbcExecutorClient(dataSourceParam, jdbcConnectionInfo);
        Connection connection = executorClient.getConnection();

        TableColumnInfo tableColumnInfo = null;
        try {
            String catalog = StringUtils.isEmpty(jdbcConnectionInfo.getSchema()) ? jdbcConnectionInfo.getCatalog() : param.getDataBase();
            String schema = StringUtils.isEmpty(jdbcConnectionInfo.getSchema()) ?  param.getDataBase() : jdbcConnectionInfo.getSchema();
            String tableName = param.getTable();
            if (null != connection) {
                DatabaseMetaData metaData = connection.getMetaData();
                List<String> primaryKeys = getPrimaryKeys(catalog, schema, tableName, metaData);
                List<ColumnInfo> columns = getColumns(catalog, schema, tableName, metaData);
                tableColumnInfo = new TableColumnInfo(tableName, primaryKeys, columns);
            }
        } catch (SQLException e) {
            logger.error("get column list error , param is {} : ", param, e);
        } finally {
            JdbcDataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableColumnInfo).build();
    }

    @Override
    public ConnectorResponse getPartitions(ConnectorRequestParam param) {
        return Connector.super.getPartitions(param);
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(param.getDataSourceParam(), JdbcConnectionInfo.class);
        BaseJdbcDataSourceInfo dataSourceInfo = getDatasourceInfo(jdbcConnectionInfo);
        dataSourceInfo.loadClass();

        try (Connection con = DriverManager.getConnection(dataSourceInfo.getJdbcUrl(), dataSourceInfo.getUser(), dataSourceInfo.getPassword())) {
            boolean result = con != null;
            if (result) {
                con.close();
            }

            return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(result).build();
        } catch (SQLException e) {
            logger.error("test connect error, param is {} :", JSONUtils.toJsonString(param), e);
        }

        return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(false).build();
    }

    private List<String> getPrimaryKeys(String catalog, String schema, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        List<String> primaryKeys = new ArrayList<>();
        try {

            rs = getPrimaryKeys(metaData, catalog, schema, tableName);

            if (rs == null) {
                return primaryKeys;
            }

            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            logger.error("get primary key error, param is {} :", schema + "." + tableName, e);
        } finally {
            JdbcDataSourceUtils.closeResult(rs);
        }
        return primaryKeys;
    }

    public List<ColumnInfo> getColumns(String catalog, String schema, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        List<ColumnInfo> columnList = new ArrayList<>();
        try {
            rs = getMetadataColumns(metaData, catalog, schema, tableName, "%");
            if (rs == null) {
                return columnList;
            }
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String rawType = rs.getString("TYPE_NAME");
                String comment = rs.getString("REMARKS");
                columnList.add(new ColumnInfo(name, rawType, comment,false));
            }
        } catch (Exception e) {
            logger.error("get column error, param is {} :", schema + "." + tableName, e);
        } finally {
            JdbcDataSourceUtils.closeResult(rs);
        }
        return columnList;
    }

    @Override
    public List<String> keyProperties() {
        return Arrays.asList(HOST, PORT, DATABASE);
    }

    protected ResultSet getMetadataDatabases(Connection connection) throws SQLException {
        java.sql.Statement stmt = connection.createStatement();
        return stmt.executeQuery("show databases");
    }

    protected ResultSet getMetadataTables(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        return metaData.getTables(catalog, schema, null, TABLE_TYPES);
    }

    protected ResultSet getMetadataColumns(DatabaseMetaData metaData,
                                                    String catalog, String schema,
                                                    String tableName, String columnName) throws SQLException {
        return metaData.getColumns(catalog, schema, tableName, columnName);
    }

    protected ResultSet getPrimaryKeys(DatabaseMetaData metaData,String catalog, String schema, String tableName) throws SQLException {
        return metaData.getPrimaryKeys(schema, null, tableName);
    }
}
