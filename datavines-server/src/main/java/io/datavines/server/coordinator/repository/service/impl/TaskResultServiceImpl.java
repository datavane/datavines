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
