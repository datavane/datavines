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
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.type.InputParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.DataSourceClient;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static io.datavines.common.ConfigConstants.PASSWORD;
import static io.datavines.common.ConfigConstants.USER;

public class TrinoConnector extends JdbcConnector {

    public TrinoConnector(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }

    @Override
    protected ResultSet getPrimaryKeys(DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getMetadataDatabases(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery("SHOW SCHEMAS");
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(JdbcConnectionInfo jdbcConnectionInfo) {
        return new TrinoDataSourceInfo(jdbcConnectionInfo);
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(param.getDataSourceParam(), JdbcConnectionInfo.class);
        BaseJdbcDataSourceInfo dataSourceInfo = getDatasourceInfo(jdbcConnectionInfo);
        dataSourceInfo.loadClass();

        Properties properties = new Properties();
        properties.setProperty(USER, dataSourceInfo.getUser());
        if (StringUtils.isNotEmpty(dataSourceInfo.getPassword())) {
            properties.setProperty(PASSWORD, dataSourceInfo.getUser());
        }

        String[] url2Array = dataSourceInfo.getJdbcUrl().split("\\?");
        String url = url2Array[0];
        if (url2Array.length > 1) {
            String[] keyArray =  url2Array[1].split("&");
            for (String prop : keyArray) {
                String[] values = prop.split("=");
                properties.setProperty(values[0], values[1]);
            }
        }

        try (Connection con = DriverManager.getConnection(url, properties)) {
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

    @Override
    public List<String> keyProperties() {
        return Arrays.asList("host","port","catalog","database");
    }
}
