package io.datavines.server.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.datavines.server.coordinator.repository.entity.Job;

@Mapper
public interface JobMapper extends BaseMapper<Job> {

    @Select("SELECT * from job WHERE datasource_id = #{datasourceId} ")
    List<Job> listByDataSourceId(long dataSourceId);

}
