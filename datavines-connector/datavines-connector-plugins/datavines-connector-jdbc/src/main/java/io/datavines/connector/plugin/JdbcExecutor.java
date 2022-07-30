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

import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.ExecuteRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.Md5Utils;
import io.datavines.connector.api.Executor;
import io.datavines.common.jdbc.datasource.*;
import io.datavines.common.jdbc.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

public abstract class JdbcExecutor implements Executor, IDataSourceInfo {

    @Override
    public ConnectorResponse executeSyncQuery(ExecuteRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplate(getDatasourceInfo(dataSourceParam));

        String sql = param.getScript();
        if(StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(SqlUtils.query(jdbcTemplate, sql, 0));

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
}
