package io.datavines.server.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.datavines.server.coordinator.repository.entity.Command;

@Mapper
public interface CommandMapper extends BaseMapper<Command> {

    /**
     * SELECT BY ID
     * @return
     */
    @Select("SELECT * from command order by update_time limit 1 ")
    Command getOne();
}
