package io.datavines.server.coordinator.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datavines.server.coordinator.repository.entity.TaskResult;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskResultMapper extends BaseMapper<TaskResult>  {

    @Select("SELECT * from task_result where task_id = #{taskId} limit 1 ")
    TaskResult getOne(Long taskId);
}
