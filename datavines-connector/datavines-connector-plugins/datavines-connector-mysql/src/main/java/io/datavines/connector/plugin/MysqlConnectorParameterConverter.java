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

import io.datavines.common.utils.StringUtils;
import static io.datavines.common.ConfigConstants.*;

public class MysqlConnectorParameterConverter extends JdbcConnectorParameterConverter {

    @Override
    protected String getUrl(Map<String, Object> parameter) {
        String url = String.format("jdbc:mysql://%s:%s/%s",
                parameter.get(HOST),
                parameter.get(PORT),
                parameter.get(DATABASE));
        String properties = (String)parameter.get(PROPERTIES);
        if (StringUtils.isNotEmpty(properties)) {
            url += "?" + properties;
        }

        return url;
    }

}
