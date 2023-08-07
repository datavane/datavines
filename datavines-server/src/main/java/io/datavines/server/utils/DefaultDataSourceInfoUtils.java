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
package io.datavines.server.utils;

import com.zaxxer.hikari.HikariDataSource;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.utils.JdbcUrlParser;
import io.datavines.common.utils.SensitiveLogUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.datavines.common.ConfigConstants.*;

public class DefaultDataSourceInfoUtils {

  public static ConnectionInfo getDefaultConnectionInfo() {
    javax.sql.DataSource defaultDataSource =
        SpringApplicationContext.getBean(javax.sql.DataSource.class);
    HikariDataSource hikariDataSource = (HikariDataSource) defaultDataSource;

    ConnectionInfo connectionInfo =
        JdbcUrlParser.getConnectionInfo(
            hikariDataSource.getJdbcUrl(),
            hikariDataSource.getUsername(),
            hikariDataSource.getPassword());

    if (connectionInfo != null) {
      connectionInfo.setDriverName(hikariDataSource.getDriverClassName());
    }

    return connectionInfo;
  }

  public static Map<String, Object> getDefaultDataSourceConfigMap(
      Boolean... passwordDesensitization) {

    ConnectionInfo connectionInfo = getDefaultConnectionInfo();

    Map<String, Object> configMap = new HashMap<>();
    configMap.put(URL, connectionInfo.getUrl());
    configMap.put(USER, connectionInfo.getUser());
    if ((!ArrayUtils.isEmpty(passwordDesensitization))
        && Stream.of(passwordDesensitization).allMatch(BooleanUtils::isTrue)) {
      configMap.put(PASSWORD, SensitiveLogUtils.maskDataSourcePwd(connectionInfo.getPassword()));
    } else {
      configMap.put(PASSWORD, connectionInfo.getPassword());
    }
    configMap.put(DRIVER, connectionInfo.getDriverName());

    return configMap;
  }
}
