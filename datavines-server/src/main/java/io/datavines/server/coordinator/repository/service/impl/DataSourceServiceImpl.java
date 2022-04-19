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

package io.datavines.server.coordinator.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.TestConnectionRequestParam;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.core.utils.JsonUtils;
import io.datavines.server.coordinator.repository.entity.DataSource;
import io.datavines.server.coordinator.repository.mapper.DataSourceMapper;
import io.datavines.server.coordinator.repository.service.DataSourceService;

import io.datavines.spi.PluginLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dataSourceService")
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource>  implements DataSourceService {

    @Override
    public boolean testConnect(TestConnectionRequestParam param) {
        ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
        ConnectorResponse response = connectorFactory.getConnector().testConnect(param);
        return (boolean)response.getResult();
    }

    @Override
    public long insert(DataSource dataSource) {
        baseMapper.insert(dataSource);
        return dataSource.getId();
    }

    @Override
    public int update(DataSource dataSource) {
        return baseMapper.updateById(dataSource);
    }

    @Override
    public DataSource getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<DataSource> listByWorkSpaceId(long workspaceId) {
        return baseMapper.selectList(new QueryWrapper<DataSource>().eq("workspace_id", workspaceId));
    }
}
