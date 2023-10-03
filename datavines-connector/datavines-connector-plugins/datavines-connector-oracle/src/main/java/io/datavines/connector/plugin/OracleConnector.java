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
import io.datavines.common.datasource.jdbc.JdbcDataSourceInfoManager;
import io.datavines.common.datasource.jdbc.JdbcExecutorClient;
import io.datavines.common.datasource.jdbc.entity.ColumnInfo;
import io.datavines.common.datasource.jdbc.entity.DatabaseInfo;
import io.datavines.common.datasource.jdbc.utils.JdbcDataSourceUtils;
import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.GetDatabasesRequestParam;
import io.datavines.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDatabaseMetaData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OracleConnector extends JdbcConnector {

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(JdbcConnectionInfo jdbcConnectionInfo) {
        return new OracleDataSourceInfo(jdbcConnectionInfo);
    }

    @Override
    public ResultSet getMetadataColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName, String columnName) throws SQLException {
        OracleDatabaseMetaData oracleMetaData = (OracleDatabaseMetaData) metaData;
        OracleConnection connection = (OracleConnection) oracleMetaData.getConnection();
        connection.setRemarksReporting(true);

        ResultSet resultSet =  oracleMetaData.getColumns(schema, null, tableName, columnName);
        return resultSet;
    }

    @Override
    public ResultSet getMetadataTables(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        //log.
        return metaData.getTables(catalog, schema, null, TABLE_TYPES);
    }

    @Override
    public ResultSet getMetadataDatabases(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery("select username from all_users");
    }



    @Override
    public List<ColumnInfo> getColumns(String catalog, String schema, String tableName, DatabaseMetaData metaData) {

        ResultSet rs = null;
        List<ColumnInfo> columnList = new ArrayList<>();
        try {
            //TODO 目前oracle版本存在拿不到column_name,要考虑下这块的内容
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
            logger.error(e.toString(), e);
        } finally {
            JdbcDataSourceUtils.closeResult(rs);
        }
        return columnList;
    }



}
