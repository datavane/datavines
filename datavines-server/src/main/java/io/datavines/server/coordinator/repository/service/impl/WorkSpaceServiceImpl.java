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
import io.datavines.common.dto.workspace.WorkSpaceCreate;
import io.datavines.common.dto.workspace.WorkSpaceUpdate;
import io.datavines.server.coordinator.api.enums.ApiStatus;
import io.datavines.server.coordinator.repository.entity.Job;
import io.datavines.server.coordinator.repository.entity.WorkSpace;
import io.datavines.server.coordinator.repository.mapper.JobMapper;
import io.datavines.server.coordinator.repository.mapper.WorkSpaceMapper;
import io.datavines.server.coordinator.repository.service.JobService;
import io.datavines.server.coordinator.repository.service.WorkSpaceService;
import io.datavines.server.exception.DataVinesServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("workSpaceService")
public class WorkSpaceServiceImpl extends ServiceImpl<WorkSpaceMapper,WorkSpace> implements WorkSpaceService {

    @Override
    public long insert(WorkSpaceCreate workSpaceCreate) throws DataVinesServerException {
        if (isWorkSpaceExist(workSpaceCreate.getName())) {
            throw new DataVinesServerException(ApiStatus.WORKSPACE_EXIST_ERROR, workSpaceCreate.getName());
        }
        WorkSpace workSpace = new WorkSpace();
        BeanUtils.copyProperties(workSpaceCreate, workSpace);

        if (baseMapper.insert(workSpace) <= 0) {
            log.info("create workspace fail : {}", workSpaceCreate);
            throw new DataVinesServerException(ApiStatus.CREATE_WORKSPACE_ERROR, workSpaceCreate.getName());
        }

        return workSpace.getId();
    }

    @Override
    public int update(WorkSpaceUpdate workSpaceUpdate) throws DataVinesServerException {

        WorkSpace workSpace = getById(workSpaceUpdate.getId());
        if ( workSpace == null) {
            throw new DataVinesServerException(ApiStatus.WORKSPACE_NOT_EXIST_ERROR, workSpaceUpdate.getName());
        }

        BeanUtils.copyProperties(workSpaceUpdate, workSpace);

        if (baseMapper.updateById(workSpace) <= 0) {
            log.info("update workspace fail : {}", workSpaceUpdate);
            throw new DataVinesServerException(ApiStatus.CREATE_WORKSPACE_ERROR, workSpaceUpdate.getName());
        }

        return 1;
    }

    @Override
    public WorkSpace getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<WorkSpace> listByUserId(Long userId) {
        return baseMapper.selectList(new QueryWrapper<WorkSpace>().eq("create_by", userId));
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }

    private boolean isWorkSpaceExist(String name) {
        WorkSpace user = baseMapper.selectOne(new QueryWrapper<WorkSpace>().eq("name", name));
        return user != null;
    }
}
