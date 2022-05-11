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
package io.datavines.engine.jdbc.transform.sql;

import io.datavines.engine.jdbc.api.entity.ResultList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class InvalidateItemsExecutor implements ITransformExecutor {

    @Override
    public ResultList execute(Connection connection, String sql, String outputTable) throws Exception {
        Statement statement = connection.createStatement();

        statement.execute("drop view if exists " + outputTable);
        statement.execute("create view " + outputTable + " as " + sql);
//        ResultSet resultSet = statement.executeQuery("select * from " + outputTable);
//        ResultList resultList = SqlUtils.getListFromResultSet(resultSet, SqlUtils.getQueryFromsAndJoins(sql));
        statement.close();
//        resultSet.close();
        return null;
    }
}
