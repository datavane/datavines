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
package io.datavines.engine.flink.jdbc.source;

import io.datavines.common.utils.StringUtils;
import io.datavines.engine.common.utils.ParserUtils;
import io.datavines.engine.flink.api.entity.FLinkColumnInfo;
import io.datavines.engine.flink.jdbc.utils.FlinkTableUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import io.datavines.common.config.Config;
import io.datavines.common.config.CheckResult;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.flink.api.FlinkRuntimeEnvironment;
import io.datavines.engine.flink.api.stream.FlinkStreamSource;
import static io.datavines.common.ConfigConstants.*;

@Slf4j
public class JdbcSource implements FlinkStreamSource {
    
    private Config config = new Config();

    private final List<FLinkColumnInfo> columns = new ArrayList<>();

    @Override
    public DataStream<Row> getData(FlinkRuntimeEnvironment environment) throws Exception {
        getRowTypeInfo(config.getString(URL), config.getString(USER), config.getString(PASSWORD), "select * from " + config.getString(TABLE));
        String createTableSql = FlinkTableUtils.generateCreateTableStatement(config.getString(DATABASE), config.getString(OUTPUT_TABLE), config.getString(TABLE), columns, config);
        log.info("source create table sql: {}", createTableSql);
        environment.getTableEnv().executeSql(createTableSql);
        return null;
    }

    private void getRowTypeInfo(String jdbcUrl, String user, String password, String query) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(USER, user);
        if (!StringUtils.isEmptyOrNullStr(password)) {
            properties.setProperty(PASSWORD, ParserUtils.decode(password));
        }

        String[] url2Array = jdbcUrl.split("\\?");
        String url = url2Array[0];
        if (url2Array.length > 1) {
            String[] keyArray =  url2Array[1].split("&");
            for (String prop : keyArray) {
                String[] values = prop.split("=");
                properties.setProperty(values[0], values[1]);
            }
        }
        try (Connection conn = DriverManager.getConnection(url, properties);
             PreparedStatement stmt = conn.prepareStatement(query);) {
            ResultSetMetaData metaData = stmt.getMetaData();
            int columnCount = metaData.getColumnCount();
            TypeInformation<?>[] types = new TypeInformation[columnCount];
            String[] fieldNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                FLinkColumnInfo columnInfo = new FLinkColumnInfo();
                String columnName = metaData.getColumnName(i);
                String columnTypeName = metaData.getColumnTypeName(i).toUpperCase();
                fieldNames[i - 1] = columnName;
                switch (columnTypeName) {
                    case "VARCHAR":
                    case "TEXT":
                    case "CHAR":
                        types[i - 1] = TypeInformation.of(String.class);
                        break;
                    case "INT":
                    case "INTEGER":
                    case "SMALLINT":
                    case "TINYINT":
                        types[i - 1] = TypeInformation.of(Integer.class);
                        break;
                    case "BIGINT":
                        types[i - 1] = TypeInformation.of(Long.class);
                        break;
                    case "DOUBLE":
                    case "FLOAT":
                    case "REAL":
                        types[i - 1] = TypeInformation.of(Double.class);
                        break;
                    case "DECIMAL":
                    case "NUMERIC":
                        types[i - 1] = TypeInformation.of(java.math.BigDecimal.class);
                        break;
                    case "DATE":
                        types[i - 1] = TypeInformation.of(java.sql.Date.class);
                        break;
                    case "TIME":
                        types[i - 1] = TypeInformation.of(java.sql.Time.class);
                        break;
                    case "TIMESTAMP":
                        types[i - 1] = TypeInformation.of(java.sql.Timestamp.class);
                        break;
                    case "BOOLEAN":
                    case "BIT":
                        types[i - 1] = TypeInformation.of(Boolean.class);
                        break;
                    case "BINARY":
                    case "VARBINARY":
                    case "LONGVARBINARY":
                        types[i - 1] = TypeInformation.of(byte[].class);
                        break;
                    default:
                        types[i - 1] = TypeInformation.of(Object.class);
                }
                columnInfo.setColumn(columnName);
                columnInfo.setDataType(columnTypeName);
                columns.add(columnInfo);
            }

            new RowTypeInfo(types, fieldNames);
        }
    }

    @Override
    public List<FLinkColumnInfo> getColumns() {
        return columns;
    }

    @Override
    public void setConfig(Config config) {
        if (config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {
        List<String> requiredOptions = Arrays.asList(URL, TABLE, USER);

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x->{
            if(!config.has(x)){
                nonExistsOptions.add(x);
            }
        });

        if (!nonExistsOptions.isEmpty()) {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream()
                            .map(option -> "[" + option + "]")
                            .collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new CheckResult(true, "");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment env) {
    }
}
