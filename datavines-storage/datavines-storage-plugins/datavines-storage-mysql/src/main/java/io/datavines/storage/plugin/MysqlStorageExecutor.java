package io.datavines.storage.plugin;/*
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

import io.datavines.common.jdbc.datasource.*;
import io.datavines.common.jdbc.utils.SqlUtils;
import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.ExecuteRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.Md5Utils;
import io.datavines.storage.api.StorageExecutor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

public class MysqlStorageExecutor implements StorageExecutor, IDataSourceInfo {

    @Override
    public ConnectorResponse executeSyncQuery(ExecuteRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplate(getDatasourceInfo(dataSourceParam));

        String sql = "select * from " + param.getScript();
        if(StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(SqlUtils.queryForPage(jdbcTemplate, sql, param.getLimit(),
                param.getPageNumber(), param.getPageSize()));

        return builder.build();
    }

    public BaseDataSourceInfo getDatasourceInfo(String param) {

        if (DataSourceInfoManager.getDatasourceInfo(param) == null) {
            String key = Md5Utils.getMd5(param, false);
            ConnectionInfo connectionInfo = JSONUtils.parseObject(param,ConnectionInfo.class);
            BaseDataSourceInfo dataSourceInfo = getDatasourceInfo(connectionInfo);
            DataSourceInfoManager.putDataSourceInfo(key,dataSourceInfo);
        }

        return DataSourceInfoManager.getDatasourceInfo(param);
    }

    @Override
    public BaseDataSourceInfo getDatasourceInfo(ConnectionInfo connectionInfo) {
        return new MysqlDataSourceInfo(connectionInfo);
    }
}
