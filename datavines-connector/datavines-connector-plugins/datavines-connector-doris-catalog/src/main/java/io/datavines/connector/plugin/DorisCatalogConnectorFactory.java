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

import io.datavines.connector.api.*;

/**
 * Doris Catalog Connector Factory
 * 工厂类，负责提供所有 Doris Catalog Connector 组件的实例
 * 通过 SPI 机制注册为 doris_catalog 类型的 Connector
 */
public class DorisCatalogConnectorFactory extends DorisConnectorFactory {

    /**
     * 返回插件标识
     * 用于 SPI 加载和前端展示
     */
    @Override
    public String getCategory() {
        return "doris_catalog";
    }

    /**
     * 获取连接器实例
     */
    @Override
    public Connector getConnector() {
        return new DorisCatalogConnector(getDataSourceClient());
    }

    /**
     * 获取 SQL 方言实例
     */
    @Override
    public Dialect getDialect() {
        return new DorisCatalogDialect();
    }

    /**
     * 获取执行器实例
     */
    @Override
    public Executor getExecutor() {
        return new DorisCatalogExecutor(getDataSourceClient());
    }

    /**
     * 获取配置构建器实例
     */
    @Override
    public ConfigBuilder getConfigBuilder() {
        return new DorisCatalogConfigBuilder();
    }

    /**
     * 获取参数转换器实例
     */
    @Override
    public ParameterConverter getConnectorParameterConverter() {
        return new DorisCatalogParameterConverter();
    }
}
