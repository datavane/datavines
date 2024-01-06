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
import org.slf4j.Logger;

import java.sql.Statement;

public class LocalRuntimeEnvironment implements RuntimeEnvironment {

    protected Logger log = LoggerFactory.getLogger(LocalRuntimeEnvironment.class);

    private ConnectionHolder sourceConnection;

    private ConnectionHolder targetConnection;

    private ConnectionHolder metadataConnection;

    private Statement currentStatement;

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

    public ConnectionHolder getSourceConnection() {
        return sourceConnection;
    }

    public void setSourceConnection(ConnectionHolder sourceConnection) {
        this.sourceConnection = sourceConnection;
    }

    public ConnectionHolder getMetadataConnection() {
        return metadataConnection;
    }

    public void setMetadataConnection(ConnectionHolder metadataConnection) {
        this.metadataConnection = metadataConnection;
    }

    public ConnectionHolder getTargetConnection() {
        return targetConnection;
    }

    public void setTargetConnection(ConnectionHolder targetConnection) {
        this.targetConnection = targetConnection;
    }

    public void close() throws Exception {
        if (currentStatement != null) {
            currentStatement.close();
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
    }

    public void setCurrentStatement(Statement statement) {
        this.currentStatement = statement;
    }
}
