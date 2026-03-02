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
import io.datavines.common.utils.StringUtils;

import java.util.Map;

import static io.datavines.common.ConfigConstants.*;

public class MaxComputeDataSourceInfo extends BaseJdbcDataSourceInfo {

    public MaxComputeDataSourceInfo(Map<String, String> param) {
        super(param);
    }

    @Override
    public String getAddress() {
        return "jdbc:odps:" + param.get(ENDPOINT);
    }

    @Override
    public String getDriverClass() {
        return "com.aliyun.odps.jdbc.OdpsDriver";
    }

    @Override
    public String getType() {
        return "maxcompute";
    }

    @Override
    protected String getSeparator() {
        return "?";
    }

    @Override
    protected void appendDatabase(StringBuilder jdbcUrl) {
        String database = getDatabase();
        if (StringUtils.isNotEmpty(database)) {
            jdbcUrl.append("?project=").append(database).append("&charset=UTF-8");
        }
    }

    @Override
    protected void appendProperties(StringBuilder jdbcUrl) {
        String otherParams = filterProperties(getProperties());
        if (StringUtils.isNotEmpty(otherParams)) {
            jdbcUrl.append("&").append(otherParams);
        }
    }

    @Override
    public String paramToString() {
        String endpoint = param.getOrDefault(ENDPOINT, "");
        String database = getDatabase() != null ? getDatabase() : "";
        String user = getUser() != null ? getUser() : "";
        String password = getPassword() != null ? getPassword() : "";
        String props = getProperties() != null ? getProperties() : "";
        return endpoint.trim() + "&" + database + "&" + user + "&" + password + "&" + props;
    }
}
