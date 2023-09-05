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

import io.datavines.common.datasource.jdbc.utils.OdpsSqlUtils;

import java.util.Map;

import static io.datavines.common.ConfigConstants.STRING_TYPE;
import static io.datavines.common.datasource.jdbc.utils.OdpsSqlUtils.OPEN_FULLSCAN;

public class OdpsDialect extends JdbcDialect {


    @Override
    public Map<String, String> getDialectKeyMap() {
        super.getDialectKeyMap();
        dialectKeyMap.put(STRING_TYPE, "STRING");
        return dialectKeyMap;
    }

    @Override
    public String getDriver() {
        return "com.aliyun.odps.jdbc.OdpsDriver";
    }


    public String getTableExistsQuery(String table) {
        return OdpsSqlUtils.appendPreSql(OPEN_FULLSCAN,String.format("SELECT * FROM %s WHERE 1=0", table));
    }

    public String getSchemaQuery(String table) {
        return  OdpsSqlUtils.appendPreSql(OPEN_FULLSCAN,String.format("SELECT * FROM %s WHERE 1=0", table));
    }

    public String getCountQuery(String table) {
        return OdpsSqlUtils.appendPreSql(OPEN_FULLSCAN,String.format("SELECT COUNT(1) FROM %s", table));
    }

    public String getSelectQuery(String table) {
        return OdpsSqlUtils.appendPreSql(OPEN_FULLSCAN,String.format("SELECT * FROM %s", table));
    }
}
