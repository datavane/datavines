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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datavines.common.CommonConstants;
import io.datavines.common.entity.QueryColumn;
import io.datavines.common.entity.TableInfo;
import io.datavines.common.param.*;
import io.datavines.common.param.form.PluginParams;
import io.datavines.common.param.form.PropsType;
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.props.InputParamsProps;
import io.datavines.common.param.form.type.InputParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.Md5Utils;
import io.datavines.connector.api.Connector;
import io.datavines.common.jdbc.datasource.*;
import io.datavines.common.jdbc.utils.DataSourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class JdbcConnector implements Connector, IDataSourceInfo {

    protected final Logger logger = LoggerFactory.getLogger(JdbcConnector.class);

    protected static final String TABLE = "TABLE";

    protected static final String DATABASE = "DATABASE";

    protected static final String VIEW = "VIEW";

    protected static final String[] TABLE_TYPES = new String[]{TABLE, VIEW};

    protected static final String TABLE_NAME = "TABLE_NAME";

    protected static final String TABLE_TYPE = "TABLE_TYPE";

    @Override
    public ConnectorResponse getDatabases(GetDatabasesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        Connection connection = dataSourceManager.getConnection(getDatasourceInfo(dataSourceParam));

        ResultSet rs = getMetadataDatabases(connection);
        List<QueryColumn> databaseList = new ArrayList<>();
        while (rs.next()) {
            databaseList.add(new QueryColumn(rs.getString(1), DATABASE));
        }
        DataSourceUtils.releaseConnection(connection);
        builder.result(databaseList);

        return builder.build();
    }

    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        Connection connection = dataSourceManager.getConnection(getDatasourceInfo(dataSourceParam));

        List<QueryColumn> tableList = null;
        ResultSet tables = null;

        try {
            DatabaseMetaData metaData = connection.getMetaData();

            String schema = null;
            try {
                schema = metaData.getConnection().getSchema();
            } catch (Throwable t) {
                t.printStackTrace();
            }

            String dbName = param.getDataBase();
            tableList = new ArrayList<>();
            tables = getMetadataTables(metaData,dbName,schema);

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
                    tableList.add(new QueryColumn(name, type, tables.getString("REMARKS")));
                }
            }

        } catch (Exception e) {
            logger.error("get table list error: {0}", e);
        } finally {
            DataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableList).build();
    }

    @Override
    public ConnectorResponse getColumns(GetColumnsRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        Connection connection = dataSourceManager.getConnection(getDatasourceInfo(dataSourceParam));

        TableInfo tableInfo = null;
        try {
            String dbName = param.getDataBase();
            String tableName = param.getTable();
            if (null != connection) {
                DatabaseMetaData metaData = connection.getMetaData();
                List<String> primaryKeys = getPrimaryKeys(dbName, tableName, metaData);
                List<QueryColumn> columns = getColumns(dbName, tableName, metaData);
                tableInfo = new TableInfo(tableName, primaryKeys, columns);
            }
        } catch (SQLException e) {
            logger.error(e.toString(), e);
            throw new SQLException(e.getMessage() + ", " + dataSourceParam);
        } finally {
            DataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableInfo).build();
    }

    @Override
    public ConnectorResponse getPartitions(ConnectorRequestParam param) {
        return Connector.super.getPartitions(param);
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        BaseDataSourceInfo dataSourceInfo = getDatasourceInfo(param.getDataSourceParam());
        dataSourceInfo.loadClass();

        try (Connection con = DriverManager.getConnection(dataSourceInfo.getJdbcUrl(), dataSourceInfo.getUser(), dataSourceInfo.getPassword())) {
            boolean result = con!=null;
            if (result) {
                con.close();
            }

            return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(result).build();
        } catch (SQLException e) {
            logger.error(e.toString(), e);
        }

        return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(false).build();
    }

    @Override
    public String getConfigJson(boolean isEn) {
        InputParam host = getInputParam("host",
                isEn ? "host":"地址",
                isEn ? "please enter host ip" : "请填入连接地址", 1, Validate.newBuilder()
                .setRequired(true).setMessage(isEn ? "please enter host ip" : "请填入连接地址")
                .build());
        InputParam port = getInputParam("port",
                isEn ? "port" : "端口",
                isEn ? "please enter port" : "请填入端口号", 1, Validate.newBuilder()
                .setRequired(true).setMessage(isEn ? "please enter port" : "请填入端口号")
                .build());
        InputParam database = getInputParam("database",
                isEn ? "database" : "数据库",
                isEn ? "please enter database" : "请填入数据库", 1, Validate.newBuilder()
                .setRequired(true).setMessage(isEn ? "please enter database" : "请填入数据库")
                .build());
        InputParam user = getInputParam("user",
                isEn ? "user" : "用户名",
                isEn ? "please enter user" : "请填入用户名", 1, Validate.newBuilder()
                .setRequired(true).setMessage(isEn ? "please enter user" : "请填入用户名")
                .build());
        InputParam password = getInputParam("password",
                isEn ? "password" : "密码",
                isEn ? "please enter password" : "请填入密码", 1, Validate.newBuilder()
                .setRequired(true).setMessage(isEn ? "please enter password" : "请填入密码")
                .build());
        InputParam properties = getInputParamNoValidate("properties",
                isEn ? "properties" : "参数",
                isEn ? "please enter properties,like key=value&key1=value1" : "请填入参数，格式为key=value&key1=value1", 2);

        List<PluginParams> params = new ArrayList<>();
        params.add(host);
        params.add(port);
        params.add(database);
        params.add(user);
        params.add(password);
        params.add(properties);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            logger.error("json parse error : {}", e.getMessage(), e);
        }

        return result;
    }

    private InputParam getInputParam(String field, String title, String placeholder, int rows, Validate validate) {
        return InputParam
                .newBuilder(field, title)
                .addValidate(validate)
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXT)
                .setRows(rows)
                .setPlaceholder(placeholder)
                .setEmit(null)
                .build();
    }

    private InputParam getInputParamNoValidate(String field, String title, String placeholder, int rows) {
        return InputParam
                .newBuilder(field, title)
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXTAREA)
                .setRows(rows)
                .setPlaceholder(placeholder)
                .setEmit(null)
                .build();
    }

    public List<String> getPrimaryKeys(String dbName, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        List<String> primaryKeys = new ArrayList<>();
        try {
            rs = metaData.getPrimaryKeys(dbName, null, tableName);

            if (rs == null) {
                return primaryKeys;
            }
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            logger.error(e.toString(), e);
        } finally {
            DataSourceUtils.closeResult(rs);
        }
        return primaryKeys;
    }

    public List<QueryColumn> getColumns(String dbName, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        List<QueryColumn> columnList = new ArrayList<>();
        try {
            rs = getMetadataColumns(metaData, dbName, null, tableName, "%");
            if (rs == null) {
                return columnList;
            }
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String rawType = rs.getString("TYPE_NAME");
                String comment = rs.getString("REMARKS");
                columnList.add(new QueryColumn(name, rawType, comment));
            }
        } catch (Exception e) {
            logger.error(e.toString(), e);
        } finally {
            DataSourceUtils.closeResult(rs);
        }
        return columnList;
    }

    protected BaseDataSourceInfo getDatasourceInfo(String param) {

        if (DataSourceInfoManager.getDatasourceInfo(param) == null) {
            String key = Md5Utils.getMd5(param, false);
            ConnectionInfo connectionInfo = JSONUtils.parseObject(param,ConnectionInfo.class);
            BaseDataSourceInfo dataSourceInfo = getDatasourceInfo(connectionInfo);
            DataSourceInfoManager.putDataSourceInfo(dataSourceInfo, key);
        }

        return DataSourceInfoManager.getDatasourceInfo(param);
    }

    public abstract ResultSet getMetadataColumns(DatabaseMetaData metaData,
                                                 String dbName, String schema,
                                                 String tableName, String columnName) throws SQLException;

    public abstract ResultSet getMetadataTables(DatabaseMetaData metaData, String dbName, String schema) throws SQLException;

    public abstract ResultSet getMetadataDatabases(Connection connection) throws SQLException;
}
