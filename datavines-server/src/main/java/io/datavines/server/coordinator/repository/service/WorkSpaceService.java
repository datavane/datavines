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
package io.datavines.server.coordinator.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.datavines.server.coordinator.api.dto.bo.workspace.InviteUserIntoWorkspace;
import io.datavines.server.coordinator.api.dto.bo.workspace.RemoveUserOutWorkspace;
import io.datavines.server.coordinator.api.dto.bo.workspace.WorkSpaceCreate;
import io.datavines.server.coordinator.api.dto.bo.workspace.WorkSpaceUpdate;
import io.datavines.server.coordinator.api.dto.vo.UserVO;
import io.datavines.server.coordinator.api.dto.vo.WorkspaceVO;
import io.datavines.server.coordinator.repository.entity.WorkSpace;
import io.datavines.core.exception.DataVinesServerException;

import java.util.List;

public interface WorkSpaceService {

    long insert(WorkSpaceCreate workSpaceCreate) throws DataVinesServerException;

    int update(WorkSpaceUpdate workSpaceUpdate) throws DataVinesServerException;

    WorkSpace getById(long id);

    List<WorkspaceVO> listByUserId();

    int deleteById(long id);

    int inviteUserIntoWorkspace(InviteUserIntoWorkspace inviteUserIntoWorkspace);

    int removeUser(RemoveUserOutWorkspace removeUserOutWorkspace);

    IPage<UserVO> listUserByWorkspaceId(Long workspaceId, Integer pageNumber, Integer pageSize);
}
