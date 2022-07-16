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
import io.datavines.server.coordinator.api.dto.vo.SlaSenderVO;
import io.datavines.server.coordinator.repository.entity.SlaSender;
import io.datavines.server.coordinator.repository.mapper.SlaSenderMapper;
import io.datavines.server.coordinator.repository.service.SlaSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SlaSenderServiceImpl extends ServiceImpl<SlaSenderMapper, SlaSender> implements SlaSenderService {

    @Autowired
    private SlaSenderMapper slaSenderMapper;

    @Override
    public IPage<SlaSenderVO> pageListSender(Long workspaceId, String searchVal, Integer pageNumber, Integer pageSize) {
        Page<Object> page = new Page<>(pageNumber, pageSize);
        Page<SlaSenderVO> result = slaSenderMapper.pageListSender(page,  workspaceId, searchVal);
        return result;
    }

    @Override
    public List<SlaSenderVO> listSenders(Long workspaceId, String searchVal, String type) {
        LambdaQueryWrapper<SlaSender> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlaSender::getWorkspaceId, workspaceId);
        if (Objects.nonNull(searchVal)){
            wrapper.like(SlaSender::getName, searchVal);
        }
        wrapper.eq(SlaSender::getType, type);
        List<SlaSender> list = list(wrapper);
        List<SlaSenderVO> result = list.stream()
                .map(x -> new SlaSenderVO(x.getId(), x.getWorkspaceId(), x.getConfig(), x.getType(), x.getName(), null, x.getUpdateTime()))
                .collect(Collectors.toList());
        return result;
    }


}
