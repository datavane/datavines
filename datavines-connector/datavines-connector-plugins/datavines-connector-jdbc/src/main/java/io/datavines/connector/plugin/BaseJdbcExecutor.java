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

import io.datavines.common.datasource.jdbc.*;
import io.datavines.common.datasource.jdbc.utils.SqlUtils;
import io.datavines.common.entity.ListWithQueryColumn;
import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.ExecuteRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.connector.api.Executor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

public abstract class BaseJdbcExecutor implements Executor, IJdbcDataSourceInfo {

    private final JdbcDataSourceClient jdbcDataSourceClient;

    public BaseJdbcExecutor(JdbcDataSourceClient jdbcDataSourceClient) {
        this.jdbcDataSourceClient = jdbcDataSourceClient;
    }

    protected ListWithQueryColumn query(JdbcTemplate jdbcTemplate, String sql, int limit) {
        return SqlUtils.query(jdbcTemplate, sql, limit);
    }

    @Override
    public ConnectorResponse queryForPage(ExecuteRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);

        JdbcTemplate jdbcTemplate = jdbcDataSourceClient.getJdbcTemplate(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(jdbcConnectionInfo)));

        String sql = param.getScript();
        if (StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(SqlUtils.queryForPage(jdbcTemplate, sql, param.getLimit(),
                param.getPageNumber(), param.getPageSize()));

        return builder.build();
    }

    @Override
    public ConnectorResponse queryForOne(ExecuteRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);
        JdbcTemplate jdbcTemplate = jdbcDataSourceClient.getJdbcTemplate(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(jdbcConnectionInfo)));

        String sql = param.getScript() + " limit 1";
        if (StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(SqlUtils.queryForPage(jdbcTemplate, sql, param.getLimit(),
                param.getPageNumber(), param.getPageSize()));

        return builder.build();
    }

    @Override
    public ConnectorResponse queryForList(ExecuteRequestParam param) throws Exception {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        JdbcConnectionInfo jdbcConnectionInfo = JSONUtils.parseObject(dataSourceParam, JdbcConnectionInfo.class);

        JdbcTemplate jdbcTemplate = jdbcDataSourceClient.getJdbcTemplate(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(jdbcConnectionInfo)));

        String sql = param.getScript();
        if (StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(query(jdbcTemplate, sql, 0));

        return builder.build();
    }
}
