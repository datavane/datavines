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
package io.datavines.engine.jdbc.api;

import io.datavines.common.config.enums.SinkType;
import io.datavines.common.config.enums.SourceType;
import io.datavines.common.config.enums.TransformType;
import io.datavines.engine.api.env.Execution;
import io.datavines.engine.jdbc.api.entity.ResultList;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.datavines.engine.api.EngineConstants.PLUGIN_TYPE;

public class JdbcExecution implements Execution<JdbcSource, JdbcTransform, JdbcSink> {

    private final JdbcRuntimeEnvironment jdbcRuntimeEnvironment;

    public JdbcExecution(JdbcRuntimeEnvironment jdbcRuntimeEnvironment){
        this.jdbcRuntimeEnvironment = jdbcRuntimeEnvironment;
    }

    @Override
    public void execute(List<JdbcSource> sources, List<JdbcTransform> transforms, List<JdbcSink> sinks) throws SQLException {
        if (CollectionUtils.isEmpty(sources)) {
            return;
        }

        sources.forEach(jdbcSource -> {
            switch (SourceType.of(jdbcSource.getConfig().getString(PLUGIN_TYPE))){
                case NORMAL:
                    jdbcRuntimeEnvironment.setSourceConnection(jdbcSource.getConnection(jdbcRuntimeEnvironment));
                    break;
                case METADATA:
                    jdbcRuntimeEnvironment.setMetadataConnection(jdbcSource.getConnection(jdbcRuntimeEnvironment));
                    break;
                default:
                    break;
            }
        });

        List<ResultList> taskResult = new ArrayList<>();
        List<ResultList> actualValue = new ArrayList<>();
        transforms.forEach(jdbcTransform -> {
            switch (TransformType.of(jdbcTransform.getConfig().getString(PLUGIN_TYPE))){
                case INVALIDATE_ITEMS:
                    jdbcTransform.process(jdbcRuntimeEnvironment);
                    break;
                case ACTUAL_VALUE:
                    ResultList actualValueResult = jdbcTransform.process(jdbcRuntimeEnvironment);
                    actualValue.add(actualValueResult);
                    taskResult.add(actualValueResult);
                    break;
                case EXPECTED_VALUE_FROM_METADATA_SOURCE:
                case EXPECTED_VALUE_FROM_SOURCE:
                case EXPECTED_VALUE_FROM_TARGET_SOURCE:
                    ResultList expectedResult = jdbcTransform.process(jdbcRuntimeEnvironment);
                    taskResult.add(expectedResult);
                    break;
                default:
                    break;
            }
        });

        sinks.forEach(jdbcSink -> {
            switch (SinkType.of(jdbcSink.getConfig().getString(PLUGIN_TYPE))){
                case ERROR_DATA:
                    jdbcSink.output(null, jdbcRuntimeEnvironment);
                    break;
                case ACTUAL_VALUE:
                    jdbcSink.output(actualValue, jdbcRuntimeEnvironment);
                    break;
                case TASK_RESULT:
                    jdbcSink.output(taskResult, jdbcRuntimeEnvironment);
                    break;
                default:
                    break;
            }
        });

        jdbcRuntimeEnvironment.close();
    }

    @Override
    public void stop() {

    }
}
