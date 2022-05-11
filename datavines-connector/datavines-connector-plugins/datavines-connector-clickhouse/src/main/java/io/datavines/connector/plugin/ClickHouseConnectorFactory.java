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

import io.datavines.connector.api.Connector;
import io.datavines.connector.api.ConnectorParameterConverter;
import io.datavines.connector.api.Dialect;

public class ClickHouseConnectorFactory extends JdbcConnectorFactory {

    @Override
    public ConnectorParameterConverter getConnectorParameterConverter() {
        return new ClickHouseConnectorParameterConverter();
    }

    @Override
    public Dialect getDialect() {
        return new ClickHouseDialect();
    }

    @Override
    public Connector getConnector() {
        return new ClickHouseConnector();
    }
}
