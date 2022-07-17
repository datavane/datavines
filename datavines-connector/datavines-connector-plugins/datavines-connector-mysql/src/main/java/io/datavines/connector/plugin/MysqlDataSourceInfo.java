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

import io.datavines.common.jdbc.datasource.BaseDataSourceInfo;
import io.datavines.common.jdbc.datasource.ConnectionInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlDataSourceInfo extends BaseDataSourceInfo {

    private final Logger logger = LoggerFactory.getLogger(MysqlDataSourceInfo.class);

    public MysqlDataSourceInfo(ConnectionInfo connectionInfo) {
        super(connectionInfo);
    }

    @Override
    public String getAddress() {
        return "jdbc:mysql://"+getHost()+":"+getPort();
    }

    @Override
    public String getDriverClass() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public String getType() {
        return "mysql";
    }

    @Override
    protected String getSeparator() {
        return "?";
    }

    @Override
    protected String filterProperties(String other){
        if(StringUtils.isBlank(other)){
            return "";
        }

        String sensitiveParam = "autoDeserialize=true";
        if(other.contains(sensitiveParam)){
            int index = other.indexOf(sensitiveParam);
            String tmp = sensitiveParam;
            char symbol = '&';
            if(index == 0 || other.charAt(index + 1) == symbol){
                tmp = tmp + symbol;
            } else if(other.charAt(index - 1) == symbol){
                tmp = symbol + tmp;
            }
            logger.warn("sensitive param : {} in properties field is filtered", tmp);
            other = other.replace(tmp, "");
        }
        logger.debug("properties : {}", other);
        return other;
    }
}
