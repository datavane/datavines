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
import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.TestConnectionRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.StringUtils;

import java.sql.*;

public class DatabendConnector extends JdbcConnector {

    protected static final String TABLE = "BASE TABLE";

    protected static final String[] TABLE_TYPES = new String[]{TABLE};

    @Override
    public ResultSet getMetadataColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName, String columnName) throws SQLException {
        return metaData.getColumns(schema, null, tableName, columnName);
    }

    @Override
    public ResultSet getMetadataTables(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        return metaData.getTables(schema, null, null, TABLE_TYPES);
    }

    @Override
    public ResultSet getMetadataDatabases(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return metaData.getCatalogs();
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(JdbcConnectionInfo jdbcConnectionInfo) {
        return new DatabendDataSourceInfo(jdbcConnectionInfo);
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(param.getDataSourceParam(), JdbcConnectionInfo.class);
        BaseJdbcDataSourceInfo dataSourceInfo = getDatasourceInfo(jdbcConnectionInfo);
        dataSourceInfo.loadClass();
        try (Connection con = DriverManager.getConnection(dataSourceInfo.getJdbcUrl(),
                dataSourceInfo.getUser(), StringUtils.isEmpty(dataSourceInfo.getPassword()) ? null : dataSourceInfo.getPassword())) {
            boolean result = (con!=null);
            if (result) {
                try {
                    getMetadataDatabases(con);
                } catch (Exception e) {
                    logger.error("create connection error", e);
                    return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(false).build();
                }

                con.close();
            }

            return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(true).build();
        } catch (SQLException e) {
            logger.error("create connection error", e);
            return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(false).build();
        }
    }

}
