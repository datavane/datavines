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
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.DataSourceClient;

import java.sql.*;
import java.util.Map;

public class DatabendConnector extends JdbcConnector {

    protected static final String TABLE = "BASE TABLE";

    protected static final String[] TABLE_TYPES = new String[]{TABLE};

    public DatabendConnector(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }

    @Override
    public ResultSet getMetadataTables(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        return metaData.getTables(catalog, schema, null, TABLE_TYPES);
    }

    @Override
    public ResultSet getMetadataDatabases(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return metaData.getCatalogs();
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new DatabendDataSourceInfo(param);
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        Map<String,String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        BaseJdbcDataSourceInfo dataSourceInfo = getDatasourceInfo(paramMap);
        dataSourceInfo.loadClass();
        try (Connection con = DriverManager.getConnection(dataSourceInfo.getJdbcUrl(),
                dataSourceInfo.getUser(), StringUtils.isEmpty(dataSourceInfo.getPassword()) ? null : dataSourceInfo.getPassword())) {
            if (con != null) {
                getMetadataDatabases(con);
            }
            return ConnectorResponse.builder()
                    .status(ConnectorResponse.Status.SUCCESS)
                    .result(true)
                    .build();
        } catch (Exception e) {
            logger.error("test connect error", e);
            return ConnectorResponse.builder()
                    .status(ConnectorResponse.Status.ERROR)
                    .result(false)
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

}
