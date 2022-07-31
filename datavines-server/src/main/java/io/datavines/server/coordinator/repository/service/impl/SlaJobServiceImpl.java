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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.api.dto.bo.sla.SlaJobCreate;
import io.datavines.server.coordinator.api.dto.bo.sla.SlaJobUpdate;
import io.datavines.server.coordinator.api.dto.vo.SlaJobVO;
import io.datavines.server.coordinator.repository.entity.SlaJob;
import io.datavines.server.coordinator.repository.mapper.SlaJobMapper;
import io.datavines.server.coordinator.repository.service.SlaJobService;
import io.datavines.server.utils.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SlaJobServiceImpl extends ServiceImpl<SlaJobMapper, SlaJob> implements SlaJobService {

    @Autowired
    private SlaJobMapper slaJobMapper;

    @Override
    public List<SlaJobVO>  listSlaJob(Long slaId) {
        List<SlaJobVO> res = slaJobMapper.listSlaJob(slaId);
        return res;
    }

    @Override
    public boolean createSlaJob(SlaJobCreate create) {
        LambdaQueryWrapper<SlaJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlaJob::getJobId,create.getJobId());
        wrapper.eq(SlaJob::getWorkspaceId, create.getWorkspaceId());
        SlaJob one = getOne(wrapper);
        if (Objects.nonNull(one)){
            log.error("SlaJob has been create {}", create);
            throw new DataVinesException("SlaJob has been create");
        }
        SlaJob slaJob = new SlaJob();
        slaJob.setSlaId(create.getSlaId());
        slaJob.setJobId(create.getJobId());
        slaJob.setWorkspaceId(create.getWorkspaceId());
        slaJob.setCreateBy(ContextHolder.getUserId());
        slaJob.setUpdateBy(ContextHolder.getUserId());
        return save(slaJob);
    }

    @Override
    public boolean updateSlaJob(SlaJobUpdate update) {
        LambdaQueryWrapper<SlaJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlaJob::getSlaId,update.getSlaId());
        wrapper.eq(SlaJob::getJobId,update.getJobId());
        SlaJob one = getOne(wrapper);
        if (Objects.nonNull(one) && !one.getId().equals(update.getId())){
            log.error("db has slajob {} is same as update slajob {}", one, update);
            throw new DataVinesException("SlaJob has been exist");
        }
        SlaJob slaJob = new SlaJob();
        slaJob.setSlaId(update.getSlaId());
        slaJob.setJobId(update.getJobId());
        slaJob.setId(update.getId());
        slaJob.setUpdateTime(LocalDateTime.now());
        slaJob.setUpdateBy(ContextHolder.getUserId());
        return updateById(slaJob);
    }
}
