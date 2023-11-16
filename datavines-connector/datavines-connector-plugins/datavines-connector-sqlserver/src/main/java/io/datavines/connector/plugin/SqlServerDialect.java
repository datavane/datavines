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

import java.util.Map;

import static io.datavines.common.ConfigConstants.*;

public class SqlServerDialect extends JdbcDialect {

    @Override
    public Map<String, String> getDialectKeyMap() {
        super.getDialectKeyMap();
        dialectKeyMap.put(STRING_TYPE, "char");
        dialectKeyMap.put(LENGTH_KEY, "len(${column})");
        dialectKeyMap.put(IF_FUNCTION_KEY, "iif");
        dialectKeyMap.put(LIMIT_TOP_50_KEY, " OFFSET 0 ROWS FETCH NEXT 50 ROWS ONLY");
        dialectKeyMap.put(IF_CASE_KEY, "iif(${column} is null, 'NULL', cast(${column} as ${string_type}))");
        return dialectKeyMap;
    }

    @Override
    public String getDriver() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    public boolean invalidateItemCanOutputToSelf() {
        return true;
    }

    @Override
    public boolean supportToBeErrorDataStorage() {
        return true;
    }
}
