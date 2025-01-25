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

import io.datavines.common.utils.StringUtils;

import java.util.Map;

import static io.datavines.common.ConfigConstants.*;

public class ImpalaParameterConverter extends JdbcParameterConverter {

    @Override
    protected String getUrl(Map<String, Object> parameter) {

        StringBuilder address = new StringBuilder();
        address.append("jdbc:hive2://");
        Object port = parameter.get(PORT);
        for (String host : parameter.get(HOST).toString().split(",")) {
            address.append(String.format("%s:%s,", host, port));
        }
        address.deleteCharAt(address.length() - 1);
        address.append("/").append(parameter.get(DATABASE));
        String properties = (String) parameter.get(PROPERTIES);
        if (StringUtils.isNotEmpty(properties)) {
            address.append(";").append(properties);
        }

        address.append(";auth=noSasl");
        return address.toString();
    }
}
