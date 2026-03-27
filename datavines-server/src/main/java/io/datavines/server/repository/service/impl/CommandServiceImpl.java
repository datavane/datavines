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
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.datavines.server.enums.CommandType;
import io.datavines.server.repository.entity.Command;
import io.datavines.server.repository.mapper.CommandMapper;
import io.datavines.server.repository.service.CommandService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.List;

@Service("commandService")
public class CommandServiceImpl extends ServiceImpl<CommandMapper, Command> implements CommandService {

    @Override
    public long insert(Command command) {
        baseMapper.insert(command);
        return command.getId();
    }

    @Override
    public int update(Command command) {
        return baseMapper.updateById(command);
    }

    @Override
    public Command getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Command getStartCommand(int totalSlot, int currentSlot) {
        return baseMapper.getStartCommand(totalSlot, currentSlot);
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public boolean deleteByJobExecutionId(Long jobExecutionId) {
        return remove(new LambdaQueryWrapper<Command>().eq(Command::getJobExecutionId, jobExecutionId));
    }

    @Override
    public List<Command> listKillCommandByExecuteHost(String executeHost) {
        List<Command> commands = list(new LambdaQueryWrapper<Command>()
                .eq(Command::getType, CommandType.STOP)
                .eq(Command::getExecuteHost, executeHost).last("limit 20"));

        if (CollectionUtils.isEmpty(commands)) {
            return new ArrayList<>();
        }

        return commands;
    }
}
