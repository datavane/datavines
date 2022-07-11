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
import io.datavines.core.enums.ApiStatus;
import io.datavines.core.exception.DataVinesServerException;

import io.datavines.server.coordinator.api.entity.dto.storage.ErrorDataStorageCreate;
import io.datavines.server.coordinator.api.entity.dto.storage.ErrorDataStorageUpdate;
import io.datavines.server.coordinator.repository.entity.ErrorDataStorage;

import io.datavines.server.coordinator.repository.mapper.ErrorDataStorageMapper;
import io.datavines.server.coordinator.repository.service.ErrorDataStorageService;
import io.datavines.server.utils.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("errorDataStorageService")
public class ErrorDataStorageServiceImpl extends ServiceImpl<ErrorDataStorageMapper,ErrorDataStorage> implements ErrorDataStorageService {

    @Override
    public long create(ErrorDataStorageCreate errorDataStorageCreate) throws DataVinesServerException {
        if (isErrorDataStorageExist(errorDataStorageCreate.getName())) {
            throw new DataVinesServerException(ApiStatus.ERROR_DATA_STORAGE_EXIST_ERROR, errorDataStorageCreate.getName());
        }
        ErrorDataStorage errorDataStorage = new ErrorDataStorage();
        BeanUtils.copyProperties(errorDataStorageCreate, errorDataStorage);
        errorDataStorage.setCreateBy(ContextHolder.getUserId());
        errorDataStorage.setCreateTime(LocalDateTime.now());
        errorDataStorage.setUpdateBy(ContextHolder.getUserId());
        errorDataStorage.setUpdateTime(LocalDateTime.now());

        if (baseMapper.insert(errorDataStorage) <= 0) {
            log.info("create errorDataStorage fail : {}", errorDataStorageCreate);
            throw new DataVinesServerException(ApiStatus.CREATE_ERROR_DATA_STORAGE_ERROR, errorDataStorageCreate.getName());
        }

        return errorDataStorage.getId();
    }

    @Override
    public int update(ErrorDataStorageUpdate errorDataStorageUpdate) throws DataVinesServerException {

        ErrorDataStorage errorDataStorage = getById(errorDataStorageUpdate.getId());
        if ( errorDataStorage == null) {
            throw new DataVinesServerException(ApiStatus.ERROR_DATA_STORAGE_NOT_EXIST_ERROR, errorDataStorageUpdate.getName());
        }

        BeanUtils.copyProperties(errorDataStorageUpdate, errorDataStorage);
        errorDataStorage.setUpdateBy(ContextHolder.getUserId());
        errorDataStorage.setUpdateTime(LocalDateTime.now());

        if (baseMapper.updateById(errorDataStorage) <= 0) {
            log.info("update errorDataStorage fail : {}", errorDataStorageUpdate);
            throw new DataVinesServerException(ApiStatus.UPDATE_ERROR_DATA_STORAGE_ERROR, errorDataStorageUpdate.getName());
        }

        return 1;
    }

    @Override
    public ErrorDataStorage getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<ErrorDataStorage> listByWorkspaceId(long workspaceId) {
        return baseMapper.selectList(new QueryWrapper<ErrorDataStorage>().eq("workspace_id", workspaceId));
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }

    private boolean isErrorDataStorageExist(String name) {
        ErrorDataStorage user = baseMapper.selectOne(new QueryWrapper<ErrorDataStorage>().eq("name", name));
        return user != null;
    }
}
