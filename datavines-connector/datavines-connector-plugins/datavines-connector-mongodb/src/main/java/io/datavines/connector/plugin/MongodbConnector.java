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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import io.datavines.common.datasource.jdbc.JdbcConnectionInfo;
import io.datavines.common.datasource.jdbc.entity.ColumnInfo;
import io.datavines.common.datasource.jdbc.entity.DatabaseInfo;
import io.datavines.common.datasource.jdbc.entity.TableColumnInfo;
import io.datavines.common.datasource.jdbc.entity.TableInfo;
import io.datavines.common.param.*;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.Connector;
import org.apache.curator.shaded.com.google.common.collect.Streams;
import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static io.datavines.common.ConfigConstants.*;

public class MongodbConnector implements Connector {

    protected final Logger logger = LoggerFactory.getLogger(MongodbConnector.class);

    private static final Set<String> SYSTEM_DATABASE = Sets.newHashSet("admin", "local", "config");

    private static final Map<String, Object> AUTHORIZED_LIST_COLLECTIONS_COMMAND = ImmutableMap.<String, Object>builder()
            .put("listCollections", 1.0)
            .put("nameOnly", true)
            .put("authorizedCollections", true)
            .build();

    private static final List<String> SYSTEM_TABLE = Arrays.asList("system.indexes", "system.users", "system.version", "system.views");


    @Override
    public ConnectorResponse getDatabases(GetDatabasesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        MongoClient mongoClient = getMongoClient(dataSourceParam);

        MongoIterable<String> mongoIterable = mongoClient.listDatabases()
                .nameOnly(true)
                .authorizedDatabasesOnly(true)
                .map(result -> result.getString("name"));

        List<DatabaseInfo> databaseInfoList = Streams.stream(mongoIterable)
                .filter(db -> !SYSTEM_DATABASE.contains(db))
                .map(db -> new DatabaseInfo(db, DATABASE))
                .collect(Collectors.toList());

        builder.result(databaseInfoList);

        return builder.build();
    }

    private MongoClient getMongoClient(String dataSourceParam) {
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        return getMongoClient(jdbcConnectionInfo);
    }

    private MongoClient getMongoClient(JdbcConnectionInfo jdbcConnectionInfo) {

        MongodbClientManager instance = MongodbClientManager.getInstance();

        return instance.getMongoClient(jdbcConnectionInfo);
    }

    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        String dataBase = param.getDataBase();

        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        if (jdbcConnectionInfo == null) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        MongoClient mongoClient = getMongoClient(jdbcConnectionInfo);
        MongoDatabase database = mongoClient.getDatabase(dataBase);
        Document cursor = database.runCommand(new Document(AUTHORIZED_LIST_COLLECTIONS_COMMAND)
                .get("cursor", Document.class));

        List<Document> firstBatch = cursor.get("firstBatch", List.class);
        List<TableInfo> tableInfos = firstBatch.stream()
                .map(document -> document.getString("name"))
                .filter(name -> !name.equals(dataBase))
                .filter(name -> !SYSTEM_TABLE.contains(name))
                .map(name -> new TableInfo(dataBase, name, "", ""))
                .collect(Collectors.toList());

        return builder.result(tableInfos).build();
    }

    @Override
    public ConnectorResponse getColumns(GetColumnsRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        if (jdbcConnectionInfo == null) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        String dataBase = param.getDataBase();
        String table = param.getTable();

        MongoClient mongoClient = getMongoClient(jdbcConnectionInfo);
        MongoDatabase db = mongoClient.getDatabase(dataBase);
        Document sortDoc = new Document("_id", -1);
        Document doc = db.getCollection(table)
                .find()
                .sort(sortDoc)
                .limit(1).first();
        List<ColumnInfo> columnInfos = new ArrayList<>();
        if (doc != null) {
            for (String key : doc.keySet()) {

                Object value = doc.get(key);
                if (!key.equals("_id")) {
                    String fieldType = guessFieldType(value);
                    if (StringUtils.isNotEmpty(fieldType)) {
                        columnInfos.add(new ColumnInfo(key, fieldType));
                    }
                }
            }
        }

        TableColumnInfo tableColumnInfo = new TableColumnInfo(table, null, columnInfos);
        builder.result(tableColumnInfo);

        return builder.build();
    }

    private String guessFieldType(Object value) {
        String fieldType = "STRING";
        if (value == null) {
            return fieldType;
        }
        if (value instanceof String) {

        }
        if (value instanceof Binary) {
            fieldType = "VARBINARY";
        } else if (value instanceof Integer || value instanceof Long) {
            fieldType = "BIGINT";
        } else if (value instanceof Boolean) {
            fieldType = "BOOLEAN";
        } else if (value instanceof Float || value instanceof Double) {
            fieldType = "DOUBLE";
        } else if (value instanceof Date) {
            fieldType = "TIMESTAMP";
        } else if (value instanceof List) {
            fieldType = "ARRAY";
        }

        return fieldType;
    }



    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(param.getDataSourceParam(), JdbcConnectionInfo.class);

        try {
            MongoClient mongoClient = getMongoClient(jdbcConnectionInfo);
            if (mongoClient == null) {
                return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(false).build();
            }
            return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(true).build();
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }

        return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(false).build();
    }

    @Override
    public List<String> keyProperties() {
        return Arrays.asList(HOST, PORT, DATABASE);
    }

    @Override
    public ConnectorResponse getPartitions(ConnectorRequestParam param) {
        return Connector.super.getPartitions(param);
    }

}
