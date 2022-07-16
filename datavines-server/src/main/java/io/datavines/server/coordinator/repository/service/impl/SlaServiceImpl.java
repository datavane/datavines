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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.server.coordinator.api.dto.vo.SlaPageVO;
import io.datavines.server.coordinator.api.dto.vo.JobVO;
import io.datavines.server.coordinator.repository.entity.Sla;
import io.datavines.server.coordinator.repository.entity.SlaJob;
import io.datavines.server.coordinator.repository.mapper.SlaMapper;
import io.datavines.server.coordinator.repository.service.SlaJobService;
import io.datavines.server.coordinator.repository.service.SlaService;
import io.datavines.spi.PluginLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class SlaServiceImpl extends ServiceImpl<SlaMapper, Sla> implements SlaService {

    @Autowired
    private SlaMapper slaMapper;

    @Autowired
    private SlaJobService slaJobService;

    @Override
    public IPage<SlaPageVO> listSlas(Long workspaceId, String searchVal, Integer pageNumber, Integer pageSize) {
        Page<JobVO> page = new Page<>(pageNumber, pageSize);
        IPage<SlaPageVO> res = slaMapper.listSlas(page, workspaceId, searchVal);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        boolean result = removeById(id);
        LambdaQueryWrapper<SlaJob> lambda = new LambdaQueryWrapper<>();
        lambda.eq(SlaJob::getSlaId, id);
        slaJobService.remove(lambda);
        return result;
    }

    @Override
    public String getSenderConfigJson(String type) {
        return PluginLoader
                .getPluginLoader(SlasHandlerPlugin.class)
                .getOrCreatePlugin(type)
                .getConfigSenderJson();
    }


    @Override
    public Set<String> getSupportPlugin(){
        Set<String> supportedPlugins = PluginLoader
                .getPluginLoader(SlasHandlerPlugin.class)
                .getSupportedPlugins();
        return supportedPlugins;
    }
}
