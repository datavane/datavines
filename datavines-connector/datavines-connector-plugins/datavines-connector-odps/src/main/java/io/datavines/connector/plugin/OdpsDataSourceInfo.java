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
import io.datavines.common.utils.StringUtils;

public class OdpsDataSourceInfo extends BaseJdbcDataSourceInfo {

    public OdpsDataSourceInfo(JdbcConnectionInfo jdbcConnectionInfo) {
        super(jdbcConnectionInfo);
    }

    @Override
    public String getAddress() {
        return "jdbc:odps:"+getHost();
    }

    @Override
    public String getDriverClass() {
        return "com.aliyun.odps.jdbc.OdpsDriver";
    }

    @Override
    public String getType() {
        return "odps";
    }

    @Override
    protected String getSeparator() {
        return "?";
    }

    @Override
    protected String filterProperties(String other){
        return other;
    }

    @Override
    public String getJdbcUrl() {
        StringBuilder jdbcUrl = new StringBuilder(getAddress());

        appendProperties(jdbcUrl);

        return jdbcUrl.toString();
    }
    /**
     * append other
     * @param jdbcUrl jdbc url
     */
    @Override
    protected void appendProperties(StringBuilder jdbcUrl) {
        String otherParams = filterProperties(getProperties());
        if (StringUtils.isNotEmpty(otherParams)) {
            jdbcUrl.append(getSeparator()).append(otherParams);
            jdbcUrl.append("&").append("project=").append(getDatabase());
        }
    }

}
