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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new TrinoDataSourceInfo(param);
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        Map<String,String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        BaseJdbcDataSourceInfo dataSourceInfo = getDatasourceInfo(paramMap);
        dataSourceInfo.loadClass();

        Properties properties = new Properties();
        properties.setProperty(USER, dataSourceInfo.getUser());
        if (StringUtils.isNotEmpty(dataSourceInfo.getPassword())) {
            properties.setProperty(PASSWORD, dataSourceInfo.getPassword());
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

    @Override
    public List<String> keyProperties() {
        return Arrays.asList("host","port","catalog","database");
    }
}
