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

import io.datavines.common.config.Config;
import io.datavines.engine.jdbc.api.entity.ResultList;
import io.datavines.engine.jdbc.api.entity.ResultListWithColumns;
import io.datavines.engine.jdbc.api.utils.FileUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class InvalidateItemsExecutor implements ITransformExecutor {

    @Override
    public ResultList execute(Connection connection, Config config) throws Exception {

        String outputTable = config.getString("invalidate_items_table");
        String sql = config.getString("sql");

        Statement statement = connection.createStatement();
        statement.execute("drop view if exists " + outputTable);
        statement.execute("create view " + outputTable + " as " + sql);

        int count = 0;
        //执行统计行数语句
        ResultSet countResultSet = statement.executeQuery("select count(1) from " + outputTable);
        if (countResultSet.next()) {
            count = countResultSet.getInt(1);
        }

        if (count > 0) {
            //根据行数进行分页查询。分批写到文件里面
            int pageSize = 1000;
            int totalPage = count/pageSize + count%pageSize>0 ? 1:0;
            for (int i=0; i<totalPage; i++) {
                ResultSet resultSet = statement.executeQuery("select * from " + outputTable +" limit "+(i * pageSize) + "," + pageSize);
                ResultListWithColumns resultList = SqlUtils.getListWithHeaderFromResultSet(resultSet, SqlUtils.getQueryFromsAndJoins("select * from " + outputTable));
                //执行文件下载到本地
                FileUtils.writeToLocal(resultList,config.getString("error_data_file_dir"),config.getString("error_data_file_name"),i==0);
                resultSet.close();
            }
        }

        statement.close();
        return null;
    }
}
