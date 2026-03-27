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

import io.datavines.common.datasource.jdbc.utils.HiveSqlUtils;
import io.datavines.connector.api.entity.ResultList;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class HiveDialect extends JdbcDialect {

    @Override
    public String getDriver() {
        return "org.apache.hive.jdbc.HiveDriver";
    }

    @Override
    public ResultList getPageFromResultSet(Statement sourceConnectionStatement, ResultSet rs,String sourceTable,  int start, int end) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        String sql = "select * from " + sourceTable +  " LIMIT " + start + ", " + (end-start);
        ResultSet errorDataResultSet = sourceConnectionStatement.executeQuery(sql);
        ResultSetMetaData metaData = rs.getMetaData();
        resultList.add(HiveSqlUtils.getResultObjectMap(errorDataResultSet, metaData));
        return new ResultList(resultList);
    }
}
