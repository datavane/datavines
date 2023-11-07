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
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

import static io.datavines.common.ConfigConstants.*;

public class MongodbConnectorParameterConverter extends JdbcConnectorParameterConverter {

    @Override
    protected String getUrl(Map<String, Object> parameter) {
        StringBuilder address = new StringBuilder();
        address.append("mongodb://");
        String user = (String) parameter.get(USER);
        if (StringUtils.isNotEmpty(user)) {
            address.append(user);
            String password = (String) parameter.get(PASSWORD);
            if (StringUtils.isNotEmpty(password)) {
                address.append(":");
                address.append(password);
            }
            address.append("@");
        }

        String port = (String) parameter.get(PORT);
        for (String host : parameter.get(HOST).toString().split(",")) {
            address.append(String.format("%s:%s,", host, port));
        }
        address.deleteCharAt(address.length() - 1);
        String database = (String) parameter.get(DATABASE);
        if (StringUtils.isNotEmpty(database)) {
            address.append("/").append(database);
        }
        String properties = (String) parameter.get(PROPERTIES);
        if (StringUtils.isNotEmpty(properties)) {
            address.append("?").append(properties);
        }
        return address.toString();
    }

    @Override
    public Map<String, Object> converter(Map<String, Object> parameter) {
        Map<String, Object> config = new HashMap<>(4);

        config.put(SPARK_MONGODB_INPUT_COLLECTION, parameter.get(TABLE));
        config.put(SPARK_MONGODB_OUTPUT_COLLECTION, parameter.get(TABLE));
        config.put(SPARK_MONGODB_INPUT_URI, parameter.get(URL) == null ? getUrl(parameter) : parameter.get(URL));
        config.put(SPARK_MONGODB_OUTPUT_URI, parameter.get(URL) == null ? getUrl(parameter) : parameter.get(URL));
        return config;
    }

    @Override
    public String getConnectorUUID(Map<String, Object> parameter) {
        return DigestUtils.md5Hex(
                String.valueOf(parameter.get(SPARK_MONGODB_INPUT_URI)) +
                        parameter.get(SPARK_MONGODB_INPUT_COLLECTION));
    }

}
