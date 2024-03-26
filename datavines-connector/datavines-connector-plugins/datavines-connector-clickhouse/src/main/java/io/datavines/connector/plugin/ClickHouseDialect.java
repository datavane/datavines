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

import static io.datavines.common.ConfigConstants.STD_DEV_KEY;
import static io.datavines.common.ConfigConstants.VARIANCE_KEY;

public class ClickHouseDialect extends JdbcDialect {

    @Override
    public String getDriver() {
        return "ru.yandex.clickhouse.ClickHouseDriver";
    }

    @Override
    public Map<String, String> getDialectKeyMap() {
        super.getDialectKeyMap();
        dialectKeyMap.put(STD_DEV_KEY, "STDDEV_POP");
        dialectKeyMap.put(VARIANCE_KEY, "VAR_POP");
        return dialectKeyMap;

    }
}
