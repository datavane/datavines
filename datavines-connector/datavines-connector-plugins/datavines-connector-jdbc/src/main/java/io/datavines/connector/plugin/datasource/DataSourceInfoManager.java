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

package io.datavines.connector.plugin.datasource;

import io.datavines.common.utils.Md5Utils;

import java.util.concurrent.ConcurrentHashMap;

public class DataSourceInfoManager {

    private static final ConcurrentHashMap<String, BaseDataSourceInfo> DATA_SOURCE_INFO_MAP =
            new ConcurrentHashMap<>();

    public static BaseDataSourceInfo getDatasourceInfo(String param) {
        BaseDataSourceInfo dataSourceInfo = null;

        String key = Md5Utils.getMd5(param, false);
        dataSourceInfo = DATA_SOURCE_INFO_MAP.get(key);

        return dataSourceInfo;
    }

    public static void putDataSourceInfo(BaseDataSourceInfo dataSourceInfo, String key) {
        DATA_SOURCE_INFO_MAP.put(key,dataSourceInfo);
    }
}
