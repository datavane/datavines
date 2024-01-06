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
import org.apache.commons.collections4.CollectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static io.datavines.common.ConfigConstants.*;

public class ActualValueExecutor implements ITransformExecutor {

    @Override
    public ResultList execute(Connection connection, Config config, LocalRuntimeEnvironment env) throws Exception {

        Statement statement = null;
        ResultSet resultSet = null;
        ResultList resultList;
        try {
            String sql = config.getString(SQL);

            statement = connection.createStatement();
            env.setCurrentStatement(statement);
            resultSet = statement.executeQuery(sql);
            resultList = SqlUtils.getListFromResultSet(resultSet, SqlUtils.getQueryFromsAndJoins(sql));
            if (CollectionUtils.isNotEmpty(resultList.getResultList())) {
                List<Map<String, Object>> dataList = resultList.getResultList();
                List<Map<String, Object>> newDataList = new ArrayList<>();

                List<String> valueList = new ArrayList<>();
                String key = new ArrayList<>(dataList.get(0).keySet()).get(0);
                for (Map<String, Object> item : dataList) {
                    valueList.addAll(item.values().stream().map(String::valueOf).collect(Collectors.toList()));
                }

                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put(key, String.join("@#@",valueList.toArray(new String[]{})));
                newDataList.add(dataMap);
                resultList.setResultList(newDataList);
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
            SqlUtils.closeStatement(statement);
            env.setCurrentStatement(null);
        }

        return resultList;
    }
}
