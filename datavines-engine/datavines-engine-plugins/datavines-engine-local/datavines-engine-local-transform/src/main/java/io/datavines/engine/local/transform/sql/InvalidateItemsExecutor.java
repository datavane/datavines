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
package io.datavines.engine.local.transform.sql;

import io.datavines.common.config.Config;
import io.datavines.engine.local.api.LocalRuntimeEnvironment;
import io.datavines.engine.local.api.entity.ResultList;
import io.datavines.engine.local.api.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.Statement;

import static io.datavines.common.ConfigConstants.*;

@Slf4j
public class InvalidateItemsExecutor implements ITransformExecutor {

    @Override
    public ResultList execute(Connection connection, Config config, LocalRuntimeEnvironment env) throws Exception {

        String outputTable = config.getString(INVALIDATE_ITEMS_TABLE);
        String sql = config.getString(SQL);
        Statement statement = null;
        try {
            statement = connection.createStatement();
            env.setCurrentStatement(statement);
            SqlUtils.dropView(outputTable, statement);
            statement.execute("CREATE VIEW " + outputTable + " AS " + sql);
        } finally {
            SqlUtils.closeStatement(statement);
            env.setCurrentStatement(null);
        }
        return null;
    }
}
