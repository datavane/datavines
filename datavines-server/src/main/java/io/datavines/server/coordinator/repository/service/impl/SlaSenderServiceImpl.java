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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.coordinator.api.dto.vo.SlaSenderVo;
import io.datavines.server.coordinator.repository.entity.SlaSender;
import io.datavines.server.coordinator.repository.mapper.SlaSenderMapper;
import io.datavines.server.coordinator.repository.service.SlaSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlaSenderServiceImpl extends ServiceImpl<SlaSenderMapper, SlaSender> implements SlaSenderService {

    @Autowired
    private SlaSenderMapper slaSenderMapper;

    @Override
    public IPage<SlaSenderVo> pageListSender(Long workSpaceId, String searchVal, Integer pageNumber, Integer pageSize) {
        Page<Object> page = new Page<>(pageNumber, pageSize);
        Page<SlaSenderVo> result = slaSenderMapper.pageListSender(page,  workSpaceId, searchVal);
        return result;
    }


}
