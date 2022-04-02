package io.datavines.server.coordinator.repository.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.server.coordinator.repository.mapper.CommandMapper;
import io.datavines.server.coordinator.repository.service.CommandService;
import io.datavines.server.coordinator.repository.entity.Command;

@Service("commandService")
public class CommandServiceImpl extends ServiceImpl<CommandMapper,Command> implements CommandService {

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
    public Command getOne() {
        return baseMapper.getOne();
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }
}
