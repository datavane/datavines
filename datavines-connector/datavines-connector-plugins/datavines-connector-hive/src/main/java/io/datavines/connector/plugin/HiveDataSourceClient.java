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

import io.datavines.common.ConfigConstants;
import io.datavines.common.datasource.jdbc.BaseJdbcDataSourceInfo;
import io.datavines.common.exception.DataVinesException;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class HiveDataSourceClient extends JdbcDataSourceClient {

    @Override
    public DataSource getDataSource(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        HiveDataSourceInfo hiveInfo = (HiveDataSourceInfo) baseJdbcDataSourceInfo;
        return HiveDriverClassLoader.getDataSource(hiveInfo.getHiveVersion(),
                hiveInfo.getJdbcUrl(), hiveInfo.getUser(), hiveInfo.getPassword());
    }

    @Override
    public Connection getConnection(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        if (KerberosUtils.checkKerberosConfig(baseJdbcDataSourceInfo.getKeytabPrincipal(), baseJdbcDataSourceInfo.getKeytabFile(), baseJdbcDataSourceInfo.getKrb5ConfFile())) {
            KerberosUtils.initKerberos(baseJdbcDataSourceInfo.getKeytabPrincipal(), baseJdbcDataSourceInfo.getKeytabFile(), baseJdbcDataSourceInfo.getKrb5ConfFile());
        }
        return getDataSource(baseJdbcDataSourceInfo).getConnection();
    }

    @Override
    public Connection getConnection(Map<String, Object> configMap, Logger logger) throws DataVinesException {
        try {
            if (KerberosUtils.checkKerberosConfig(String.valueOf(configMap.get(ConfigConstants.KEYTAB_PRINCIPAL)), String.valueOf(configMap.get(ConfigConstants.KEYTAB_FILE)), String.valueOf(configMap.get(ConfigConstants.KRB5_CONF)))) {
                KerberosUtils.initKerberos(String.valueOf(configMap.get(ConfigConstants.KEYTAB_PRINCIPAL)), String.valueOf(configMap.get(ConfigConstants.KEYTAB_FILE)), String.valueOf(configMap.get(ConfigConstants.KRB5_CONF)));
            }

            String hiveVersion = configMap.get(ConfigConstants.HIVE_VERSION) != null
                    ? String.valueOf(configMap.get(ConfigConstants.HIVE_VERSION)) : "2.1";
            String url = String.valueOf(configMap.get("url"));
            String user = String.valueOf(configMap.get("user"));
            String password = String.valueOf(configMap.get("password"));

            DataSource dataSource = HiveDriverClassLoader.getDataSource(hiveVersion, url, user, password);
            if (dataSource != null) {
                Connection connection = dataSource.getConnection();
                logger.info("get connection success : {}", url + "[username=" + user + "]");
                return connection;
            } else {
                logger.error("get datasource error");
                throw new DataVinesException("can not get datasource");
            }
        } catch (SQLException exception) {
            logger.error("get connection error :", exception);
            throw new DataVinesException(exception);
        }
    }

    @Override
    public JdbcTemplate getJdbcTemplate(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource(baseJdbcDataSourceInfo));
        jdbcTemplate.setFetchSize(500);
        return jdbcTemplate;
    }

    @Override
    public DataSource getDataSource(Map<String, Object> configMap) throws SQLException {
        throw new UnsupportedOperationException(
                "Hive connector requires ClassLoader isolation. Use getDataSource(BaseJdbcDataSourceInfo) instead.");
    }

    @Override
    public Connection getConnection(Map<String, Object> configMap) throws SQLException {
        throw new UnsupportedOperationException(
                "Hive connector requires ClassLoader isolation. Use getConnection(BaseJdbcDataSourceInfo) instead.");
    }

    @Override
    public DataSource getDataSource(Properties properties) throws SQLException {
        throw new UnsupportedOperationException(
                "Hive connector requires ClassLoader isolation. Use getDataSource(BaseJdbcDataSourceInfo) instead.");
    }

    @Override
    public Connection getConnection(Properties properties) throws SQLException {
        throw new UnsupportedOperationException(
                "Hive connector requires ClassLoader isolation. Use getConnection(BaseJdbcDataSourceInfo) instead.");
    }
}
