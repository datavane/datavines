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
package io.datavines.engine.core.config;

import io.datavines.common.config.ConfigRuntimeException;
import io.datavines.common.config.DataVinesJobConfig;
import io.datavines.common.config.EnvConfig;
import io.datavines.engine.core.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(ConfigParser.class);

    private final String configFile;

    private final DataVinesJobConfig config;

    private final EnvConfig envConfig;

    public ConfigParser(String configFile){
        this.configFile = configFile;
        this.config = load();
        this.envConfig = config.getEnvConfig();
    }

    private DataVinesJobConfig load() {

        if (configFile.isEmpty()) {
            throw new ConfigRuntimeException("Please specify config file");
        }

        logger.info("Loading config file: " + configFile);

        DataVinesJobConfig config = JsonUtils.parseObject(configFile, DataVinesJobConfig.class);

        logger.info("config after parse: " + JsonUtils.toJsonString(config));

        return config;
    }

    public DataVinesJobConfig getConfig() {
        return config;
    }

    public EnvConfig getEnvConfig() {
        return envConfig;
    }
}
