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
import io.datavines.server.coordinator.api.dto.bo.workspace.InviteUserIntoWorkspace;
import io.datavines.server.coordinator.api.dto.bo.workspace.RemoveUserOutWorkspace;
import io.datavines.server.coordinator.api.dto.bo.workspace.WorkSpaceCreate;
import io.datavines.server.coordinator.api.dto.bo.workspace.WorkSpaceUpdate;
import io.datavines.server.coordinator.api.dto.vo.WorkspaceVO;
import io.datavines.server.coordinator.repository.entity.User;
import io.datavines.server.coordinator.repository.entity.UserWorkspace;
import io.datavines.server.coordinator.repository.entity.WorkSpace;
import io.datavines.server.coordinator.repository.mapper.UserMapper;
import io.datavines.server.coordinator.repository.mapper.UserWorkspaceMapper;
import io.datavines.server.coordinator.repository.mapper.WorkSpaceMapper;
import io.datavines.server.coordinator.repository.service.WorkSpaceService;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.utils.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("workSpaceService")
public class WorkSpaceServiceImpl extends ServiceImpl<WorkSpaceMapper,WorkSpace> implements WorkSpaceService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserWorkspaceMapper userWorkspaceMapper;

    @Override
    public long insert(WorkSpaceCreate workSpaceCreate) throws DataVinesServerException {
        if (isWorkSpaceExist(workSpaceCreate.getName())) {
            throw new DataVinesServerException(ApiStatus.WORKSPACE_EXIST_ERROR, workSpaceCreate.getName());
        }
        WorkSpace workSpace = new WorkSpace();
        BeanUtils.copyProperties(workSpaceCreate, workSpace);
        workSpace.setCreateBy(ContextHolder.getUserId());
        workSpace.setCreateTime(LocalDateTime.now());
        workSpace.setUpdateBy(ContextHolder.getUserId());
        workSpace.setUpdateTime(LocalDateTime.now());

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
        workSpace.setUpdateBy(ContextHolder.getUserId());
        workSpace.setUpdateTime(LocalDateTime.now());

        if (baseMapper.updateById(workSpace) <= 0) {
            log.info("update workspace fail : {}", workSpaceUpdate);
            throw new DataVinesServerException(ApiStatus.UPDATE_WORKSPACE_ERROR, workSpaceUpdate.getName());
        }

        return 1;
    }

    @Override
    public WorkSpace getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<WorkspaceVO> listByUserId() {
        return userWorkspaceMapper.listWorkspaceByUserId(ContextHolder.getUserId());
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }

    private boolean isWorkSpaceExist(String name) {
        WorkSpace user = baseMapper.selectOne(new QueryWrapper<WorkSpace>().eq("name", name));
        return user != null;
    }

    @Override
    public int inviteUserIntoWorkspace(InviteUserIntoWorkspace inviteUserIntoWorkspace) {

        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username",inviteUserIntoWorkspace.getUsername())
                .eq("email",inviteUserIntoWorkspace.getEmail()));

        if (user == null) {
            throw new DataVinesServerException(ApiStatus.USER_IS_NOT_EXIST_ERROR);
        }

        UserWorkspace userWorkspace = userWorkspaceMapper.selectOne(new QueryWrapper<UserWorkspace>()
                .eq("user_id",user.getId())
                .eq("workspace_id",inviteUserIntoWorkspace.getWorkspaceId()));

        if (userWorkspace != null) {
            throw new DataVinesServerException(ApiStatus.USER_IS_IN_WORKSPACE_ERROR);
        }

        userWorkspace = new UserWorkspace();
        userWorkspace.setUserId(user.getId());
        userWorkspace.setWorkspaceId(inviteUserIntoWorkspace.getWorkspaceId());
        userWorkspace.setCreateBy(ContextHolder.getUserId());
        userWorkspace.setCreateTime(LocalDateTime.now());
        userWorkspace.setUpdateBy(ContextHolder.getUserId());
        userWorkspace.setUpdateTime(LocalDateTime.now());

        return userWorkspaceMapper.insert(userWorkspace);
    }

    @Override
    public int removeUser(RemoveUserOutWorkspace removeUserOutWorkspace) {
        return userWorkspaceMapper.delete(new QueryWrapper<UserWorkspace>()
                .eq("user_id",removeUserOutWorkspace.getUserId()).eq("workspace_id", removeUserOutWorkspace.getWorkspaceId()));
    }
}
