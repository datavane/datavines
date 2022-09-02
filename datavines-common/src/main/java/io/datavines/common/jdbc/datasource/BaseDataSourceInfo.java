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
package io.datavines.common.jdbc.datasource;

import io.datavines.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * data source base class
 */
public abstract class BaseDataSourceInfo {

    private static final Logger logger = LoggerFactory.getLogger(BaseDataSourceInfo.class);

    protected final ConnectionInfo connectionInfo;

    public BaseDataSourceInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getUser() {
        return connectionInfo.getUser();
    }

    public String getPassword() {
        return connectionInfo.getPassword();
    }

    public String getHost() {
        return connectionInfo.getHost();
    }

    public int getPort() {
        return connectionInfo.getPort();
    }

    public abstract String getAddress();

    public abstract String getCatalog();

    public abstract String getSchema();

    public String getDatabase() {
        return connectionInfo.getDatabase();
    }

    public String getProperties() {
        return connectionInfo.getProperties();
    }

    /**
     * @return driver class
     */
    public abstract String getDriverClass();

    /**
     * @return db type
     */
    public abstract String getType();

    /**
     * gets the JDBC url for the data source connection
     * @return getJdbcUrl
     */
    public String getJdbcUrl() {
        StringBuilder jdbcUrl = new StringBuilder(getAddress());

        appendDatabase(jdbcUrl);
        appendProperties(jdbcUrl);

        return jdbcUrl.toString();
    }

    /**
     * append database
     * @param jdbcUrl jdbc url
     */
    protected void appendDatabase(StringBuilder jdbcUrl) {
        if (getAddress().lastIndexOf('/') != (jdbcUrl.length() - 1)) {
            jdbcUrl.append("/");
        }
        jdbcUrl.append(getDatabase());
    }

    /**
     * append other
     * @param jdbcUrl jdbc url
     */
    private void appendProperties(StringBuilder jdbcUrl) {
        String otherParams = filterProperties(getProperties());
        if (StringUtils.isNotEmpty(otherParams)) {
            jdbcUrl.append(getSeparator()).append(otherParams);
        }
    }

    protected abstract String getSeparator();

    /**
     * the data source test connection
     * @return Connection
     * @throws Exception Exception
     */
    public Connection getConnection() throws Exception {
        Class.forName(getDriverClass());
        return DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
    }

    protected String filterProperties(String otherParams) {
        return otherParams;
    }

    public void loadClass() {
        try {
            Class.forName(getDriverClass());
        } catch (ClassNotFoundException e) {
            logger.error("load driver error" , e);
        }
    }

    public String getUniqueKey() {
        return connectionInfo.getUniqueKey();
    }
}
