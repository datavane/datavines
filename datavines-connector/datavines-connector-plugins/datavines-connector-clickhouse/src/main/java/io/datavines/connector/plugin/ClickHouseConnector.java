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

import io.datavines.common.jdbc.datasource.BaseDataSourceInfo;
import io.datavines.common.jdbc.datasource.ConnectionInfo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClickHouseConnector extends JdbcConnector {

    @Override
    public BaseDataSourceInfo getDatasourceInfo(ConnectionInfo connectionInfo) {
        return new ClickHouseDataSourceInfo(connectionInfo);
    }

    @Override
    public ResultSet getMetadataColumns(DatabaseMetaData metaData, String dbName, String schema, String tableName, String columnName) throws SQLException {
        return metaData.getColumns(null, dbName, tableName, "%");
    }

    @Override
    public ResultSet getMetadataTables(DatabaseMetaData metaData, String dbName, String schema) throws SQLException {
        return metaData.getTables(null, dbName, null, TABLE_TYPES);
    }

    @Override
    public ResultSet getMetadataDatabases(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return metaData.getSchemas();
    }
}
