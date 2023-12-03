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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.common.enums.JobType;
import io.datavines.server.api.dto.bo.sla.SlaJobCreateOrUpdate;
import io.datavines.server.api.dto.vo.SlaJobVO;
import io.datavines.server.repository.entity.SlaJob;
import io.datavines.server.repository.mapper.SlaJobMapper;
import io.datavines.server.repository.service.SlaJobService;
import io.datavines.server.utils.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SlaJobServiceImpl extends ServiceImpl<SlaJobMapper, SlaJob> implements SlaJobService {

    @Override
    public IPage<SlaJobVO> pageSlaJob(Long slaId, String searchVal, Integer pageNumber, Integer pageSize) {
        Page<SlaJobVO> page = new Page<>(pageNumber, pageSize);
        IPage<SlaJobVO> slaJobs = baseMapper.getSlaJobPage(page, searchVal, slaId);
        List<SlaJobVO> slaJobList = slaJobs.getRecords();
        if (CollectionUtils.isNotEmpty(slaJobList)) {
            for (SlaJobVO slaJobVO: slaJobList) {
                slaJobVO.setType(JobType.of(Integer.parseInt(slaJobVO.getType())).getDescription());
            }
        }
        return slaJobs;
    }

    @Override
    public List<SlaJobVO> listSlaJob(Long slaId) {
        List<SlaJobVO> slaJobList = baseMapper.listSlaJob(slaId);
        if (CollectionUtils.isNotEmpty(slaJobList)) {
            for (SlaJobVO slaJobVO: slaJobList) {
                slaJobVO.setType(JobType.of(Integer.parseInt(slaJobVO.getType())).getDescription());
            }
        }
        return slaJobList;
    }

    @Override
    public boolean createOrUpdateSlaJob(SlaJobCreateOrUpdate createOrUpdate) {
        LambdaQueryWrapper<SlaJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlaJob::getJobId,createOrUpdate.getJobId());
        wrapper.eq(SlaJob::getWorkspaceId, createOrUpdate.getWorkspaceId());
        remove(wrapper);

        SlaJob slaJob = new SlaJob();
        BeanUtils.copyProperties(createOrUpdate, slaJob);
        slaJob.setCreateBy(ContextHolder.getUserId());
        slaJob.setUpdateBy(ContextHolder.getUserId());
        slaJob.setUpdateTime(LocalDateTime.now());
        return save(slaJob);
    }

    @Override
    public int deleteByJobId(Long id) {
        List<SlaJob> slaJobs = baseMapper.selectList(new QueryWrapper<SlaJob>().eq("job_id", id));
        if (CollectionUtils.isNotEmpty(slaJobs)) {
            List<Long> ids = slaJobs.stream()
                    .map(SlaJob::getId)
                    .collect(Collectors.toList());
            if (baseMapper.deleteBatchIds(ids) > 0) {
                return 1;
            }
        }
        return 0;
    }
}
