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
package io.datavines.engine.config;

import io.datavines.common.config.DataVinesQualityConfig;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.entity.JobExecutionInfo;
import io.datavines.common.exception.DataVinesException;
import io.datavines.spi.SPI;

import java.util.Map;

@SPI
public interface DataQualityConfigurationBuilder {

    void init(Map<String, String> inputParameter, JobExecutionInfo jobExecutionInfo);

    void buildName();

    void buildEnvConfig();

    void buildSourceConfigs() throws DataVinesException;

    void buildTransformConfigs();

    void buildSinkConfigs() throws DataVinesException;

    DataVinesQualityConfig build();
}
