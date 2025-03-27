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
import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.TestConnectionRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.connector.api.DataSourceClient;

import java.sql.*;
import java.util.Map;

public class HiveConnector extends JdbcConnector {

    public HiveConnector(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new HiveDataSourceInfo(param);
    }

    @Override
    public ResultSet getMetadataColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName, String columnName) throws SQLException {
        return metaData.getColumns(null, catalog, tableName, columnName);
    }

    @Override
    protected ResultSet getMetadataTables(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        return metaData.getTables(null, catalog, null, TABLE_TYPES);
    }

    @Override
    protected ResultSet getPrimaryKeys(DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {
        return null;
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        Map<String,String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        BaseJdbcDataSourceInfo dataSourceInfo = getDatasourceInfo(paramMap);
        if(KerberosUtils.checkKerberosConfig(dataSourceInfo.getKeytabPrincipal(), dataSourceInfo.getKeytabFile(), dataSourceInfo.getKrb5ConfFile())){
            KerberosUtils.initKerberos(dataSourceInfo.getKeytabPrincipal(), dataSourceInfo.getKeytabFile(), dataSourceInfo.getKrb5ConfFile());
        }
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

}
