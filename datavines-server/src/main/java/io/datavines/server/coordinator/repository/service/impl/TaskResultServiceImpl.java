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

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.coordinator.repository.entity.TaskResult;
import io.datavines.server.coordinator.repository.mapper.TaskResultMapper;
import io.datavines.server.coordinator.repository.service.TaskResultService;

@Service("taskResultService")
public class TaskResultServiceImpl extends ServiceImpl<TaskResultMapper, TaskResult>  implements TaskResultService {

    @Override
    public long insert(TaskResult taskResult) {
        baseMapper.insert(taskResult);
        return taskResult.getId();
    }

    @Override
    public int update(TaskResult taskResult) {
        return baseMapper.updateById(taskResult);
    }

    @Override
    public int deleteByTaskId(long taskId) {
        return baseMapper.delete(new QueryWrapper<TaskResult>().eq("task_id",taskId));
    }

    @Override
    public TaskResult getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public TaskResult getByTaskId(long taskId) {
        return baseMapper.getOne(taskId);
    }
}
