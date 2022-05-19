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

import io.datavines.common.dto.datasource.ExecuteRequest;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.param.*;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.common.dto.datasource.DataSourceCreate;
import io.datavines.common.dto.datasource.DataSourceUpdate;
import io.datavines.server.coordinator.api.enums.ApiStatus;
import io.datavines.server.coordinator.repository.entity.DataSource;
import io.datavines.server.coordinator.repository.mapper.DataSourceMapper;
import io.datavines.server.coordinator.repository.service.DataSourceService;

import io.datavines.server.exception.DataVinesServerException;
import io.datavines.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("dataSourceService")
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource>  implements DataSourceService {

    @Override
    public boolean testConnect(TestConnectionRequestParam param) {
        ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
        ConnectorResponse response = connectorFactory.getConnector().testConnect(param);
        return (boolean)response.getResult();
    }

    @Override
    public long insert(DataSourceCreate dataSourceCreate) {
        DataSource dataSource = new DataSource();
        BeanUtils.copyProperties(dataSourceCreate, dataSource);
        dataSource.setCreateTime(LocalDateTime.now());
        dataSource.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(dataSource);
        return dataSource.getId();
    }

    @Override
    public int update(DataSourceUpdate dataSourceUpdate) throws DataVinesException {
        DataSource dataSource = baseMapper.selectById(dataSourceUpdate.getId());
        if (dataSource == null){
            throw new DataVinesException("can not find the datasource");
        }

        BeanUtils.copyProperties(dataSourceUpdate, dataSource);
        dataSource.setUpdateTime(LocalDateTime.now());

        return baseMapper.updateById(dataSource);
    }

    @Override
    public DataSource getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int delete(long id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public List<DataSource> listByWorkSpaceId(long workspaceId) {
        return baseMapper.selectList(new QueryWrapper<DataSource>().eq("workspace_id", workspaceId));
    }

    @Override
    public Object getDatabaseList(Long id) throws DataVinesServerException {

        DataSource dataSource = getById(id);
        GetDatabasesRequestParam param = new GetDatabasesRequestParam();
        param.setType(dataSource.getType());
        param.setDataSourceParam(dataSource.getParam());

        Object result = null;
        ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
        try {
            ConnectorResponse response = connectorFactory.getConnector().getDatabases(param);
            result = response.getResult();
        } catch (SQLException e) {
            log.error(MessageFormat.format(ApiStatus.GET_DATABASE_LIST_ERROR.getMsg(), dataSource.getName()), e);
            throw new DataVinesServerException(ApiStatus.GET_DATABASE_LIST_ERROR, dataSource.getName());
        }

        return result;
    }

    @Override
    public Object getTableList(Long id, String database) throws DataVinesServerException {
        DataSource dataSource = getById(id);
        GetTablesRequestParam param = new GetTablesRequestParam();
        param.setType(dataSource.getType());
        param.setDataSourceParam(dataSource.getParam());
        param.setDataBase(database);

        Object result = null;
        ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
        try {
            ConnectorResponse response = connectorFactory.getConnector().getTables(param);
            result = response.getResult();
        } catch (SQLException e) {
            log.error(MessageFormat.format(ApiStatus.GET_TABLE_LIST_ERROR.getMsg(), dataSource.getName(), database), e);
            throw new DataVinesServerException(ApiStatus.GET_TABLE_LIST_ERROR, dataSource.getName(), database);
        }

        return result;
    }

    @Override
    public Object getColumnList(Long id, String database, String table) throws DataVinesServerException {
        DataSource dataSource = getById(id);
        GetColumnsRequestParam param = new GetColumnsRequestParam();
        param.setType(dataSource.getType());
        param.setDataSourceParam(dataSource.getParam());
        param.setDataBase(database);
        param.setTable(table);

        Object result = null;
        ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
        try {
            ConnectorResponse response = connectorFactory.getConnector().getColumns(param);
            result = response.getResult();
        } catch (SQLException e) {
            log.error(MessageFormat.format(ApiStatus.GET_COLUMN_LIST_ERROR.getMsg(), dataSource.getName(), database, table), e);
            throw new DataVinesServerException(ApiStatus.GET_COLUMN_LIST_ERROR, dataSource.getName(), database, table);
        }

        return result;
    }

    @Override
    public Object executeScript(ExecuteRequest request) throws DataVinesServerException {
        DataSource dataSource = getById(request.getDatasourceId());
        ExecuteRequestParam param = new ExecuteRequestParam();
        param.setType(dataSource.getType());
        param.setDataSourceParam(dataSource.getParam());
        param.setScript(request.getScript());
        Object result = null;
        ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
        try {
            ConnectorResponse response = connectorFactory.getExecutor().executeSyncQuery(param);
            result = response.getResult();
        } catch (SQLException e) {
            log.error(MessageFormat.format(ApiStatus.EXECUTE_SCRIPT_ERROR.getMsg(), request.getScript()), e);
            throw new DataVinesServerException(ApiStatus.GET_TABLE_LIST_ERROR, request.getScript());
        }

        return result;
    }

    @Override
    public String getConfigJson(String type) {
        return PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(type).getConnector().getConfigJson();
    }
}
