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
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.connector.api.TypeConverter;
import io.datavines.engine.local.api.entity.ResultList;
import io.datavines.engine.local.api.entity.ResultListWithColumns;
import io.datavines.engine.local.api.utils.FileUtils;
import io.datavines.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static io.datavines.engine.api.ConfigConstants.*;

@Slf4j
public class InvalidateItemsExecutor implements ITransformExecutor {

    @Override
    public ResultList execute(Connection connection, Config config) throws Exception {

        String outputTable = config.getString(INVALIDATE_ITEMS_TABLE);
        String sql = config.getString(SQL);
        String columnSeparator = config.getString(COLUMN_SEPARATOR);

        Statement statement = connection.createStatement();
        statement.execute("DROP VIEW IF EXISTS " + outputTable);
        statement.execute("CREATE VIEW " + outputTable + " AS " + sql);

        if (TRUE.equals(config.getString(INVALIDATE_ITEM_CAN_OUTPUT))) {
            int count = 0;
            //执行统计行数语句
            ResultSet countResultSet = statement.executeQuery("SELECT COUNT(1) FROM " + outputTable);
            if (countResultSet.next()) {
                count = countResultSet.getInt(1);
            }

            String srcConnectorType = config.getString(SRC_CONNECTOR_TYPE);
            TypeConverter typeConverter = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(srcConnectorType).getTypeConverter();
            if (count > 0) {
                //根据行数进行分页查询。分批写到文件里面
                int pageSize = 1000;
                int totalPage = count/pageSize + count%pageSize>0 ? 1:0;

                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + outputTable);

                for (int i=0; i<totalPage; i++) {
                    int start = i * pageSize;
                    int end = (i+1) * pageSize;

                    ResultListWithColumns resultList = SqlUtils.getListWithHeaderFromResultSet(resultSet, SqlUtils.getQueryFromsAndJoins("select * from " + outputTable), start, end);
                    //执行文件下载到本地
                    FileUtils.writeToLocal(resultList,
                            config.getString(ERROR_DATA_DIR),
                            config.getString(ERROR_DATA_FILE_NAME),
                            i==0,
                            typeConverter,
                            columnSeparator);
                    resultSet.close();
                }
            }
        }

        statement.close();
        return null;
    }
}
