package io.datavines.server.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.datavines.server.coordinator.repository.entity.Task;

@Mapper
public interface TaskMapper extends BaseMapper<Task>  {

    @Select("SELECT * from task WHERE datasource_id = #{dataSourceId} ")
    List<Task> listByDataSourceId(long dataSourceId);
}
