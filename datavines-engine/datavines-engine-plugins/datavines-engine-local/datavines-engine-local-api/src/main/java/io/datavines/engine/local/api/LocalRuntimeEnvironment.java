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
package io.datavines.engine.local.api;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.engine.api.env.Execution;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.local.api.entity.ConnectionHolder;
import io.datavines.engine.local.api.utils.LoggerFactory;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import java.sql.Statement;

public class LocalRuntimeEnvironment implements RuntimeEnvironment {

    protected Logger log = LoggerFactory.getLogger(LocalRuntimeEnvironment.class);

    @Setter
    @Getter
    private ConnectionHolder sourceConnection;

    @Setter
    @Getter
    private ConnectionHolder targetConnection;

    @Setter
    @Getter
    private ConnectionHolder metadataConnection;

    @Setter
    private Statement currentStatement;

    @Getter
    private boolean stop;

    @Override
    public void prepare() {

    }

    @Override
    public Execution getExecution() {
        return new LocalExecution(this);
    }

    @Override
    public void setConfig(Config config) {

    }

    @Override
    public Config getConfig() {
        return null;
    }

    @Override
    public CheckResult checkConfig() {
        return null;
    }

    public void close() throws Exception {
        if (currentStatement != null) {
            currentStatement.cancel();
        }

        if (sourceConnection != null) {
            sourceConnection.close();
        }

        if (targetConnection != null) {
            targetConnection.close();
        }

        if (metadataConnection != null) {
            metadataConnection.close();
        }

        stop = true;
    }

}
