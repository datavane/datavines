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
package io.datavines.server.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.core.enums.Status;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.api.dto.bo.config.ConfigCreate;
import io.datavines.server.api.dto.bo.config.ConfigUpdate;
import io.datavines.server.api.dto.vo.ConfigVO;
import io.datavines.server.repository.entity.Config;
import io.datavines.server.repository.mapper.ConfigMapper;
import io.datavines.server.repository.service.ConfigService;
import io.datavines.server.utils.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("configService")
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Override
    public long create(ConfigCreate configCreate) throws DataVinesServerException {
        if (isConfigExist(configCreate.getVarKey())) {
            throw new DataVinesServerException(Status.CONFIG_EXIST_ERROR, configCreate.getVarKey());
        }
        Config config = new Config();
        BeanUtils.copyProperties(configCreate, config);
        config.setCreateBy(ContextHolder.getUserId());
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateBy(ContextHolder.getUserId());
        config.setUpdateTime(LocalDateTime.now());

        if (baseMapper.insert(config) <= 0) {
            log.info("create config fail : {}", configCreate);
            throw new DataVinesServerException(Status.CREATE_CONFIG_ERROR, configCreate.getVarKey());
        }

        CommonPropertyUtils.setValue(config.getVarKey(), config.getVarValue());

        return config.getId();
    }

    @Override
    public int update(ConfigUpdate configUpdate) throws DataVinesServerException {

        Config config = getById(configUpdate.getId());
        if ( config == null) {
            throw new DataVinesServerException(Status.CONFIG_NOT_EXIST_ERROR, configUpdate.getVarKey());
        }

        BeanUtils.copyProperties(configUpdate, config);
        config.setUpdateBy(ContextHolder.getUserId());
        config.setUpdateTime(LocalDateTime.now());

        if (baseMapper.updateById(config) <= 0) {
            log.info("update config fail : {}", configUpdate);
            throw new DataVinesServerException(Status.UPDATE_CONFIG_ERROR, configUpdate.getVarKey());
        }

        CommonPropertyUtils.setValue(config.getVarKey(), config.getVarValue());
        return 1;
    }

    @Override
    public Config getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public IPage<ConfigVO> configPage(Long workspaceId, String searchVal, Integer pageNumber, Integer pageSize) {
        Page<ConfigVO> page = new Page<>(pageNumber, pageSize);
        return baseMapper.configPage(page, workspaceId, searchVal);
    }

    @Override
    public int deleteById(long id) {
        Config config = getById(id);
        if (config == null) {
            return 0;
        }

        if (config.getIsDefault() != null && config.getIsDefault()) {
            log.info("can not delete default config : {}", config.getVarKey());
            throw new DataVinesServerException(Status.CAN_NOT_DELETE_DEFAULT_CONFIG_ERROR, config.getVarKey());
        }

        if (baseMapper.deleteById(id) > 0) {
            CommonPropertyUtils.remove(config.getVarKey());
        }

        return 1;
    }

    private boolean isConfigExist(String name) {
        Config user = baseMapper.selectOne(new QueryWrapper<Config>().lambda().eq(Config::getVarKey, name));
        return user != null;
    }

    @Override
    public List<Config> listConfig() {
        return list();
    }

    @Override
    public void refreshCommonProperties() {
        List<Config> configList = listConfig();
        if (CollectionUtils.isNotEmpty(configList)) {
            configList.forEach(config -> {
                CommonPropertyUtils.setValue(config.getVarKey(), config.getVarValue());
            });
        }
    }
}
