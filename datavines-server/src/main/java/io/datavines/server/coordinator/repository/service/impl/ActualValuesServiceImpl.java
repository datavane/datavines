package io.datavines.server.coordinator.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.coordinator.repository.entity.ActualValues;
import io.datavines.server.coordinator.repository.mapper.ActualValuesMapper;
import io.datavines.server.coordinator.repository.service.ActualValuesService;
import org.springframework.stereotype.Service;

@Service("actualValuesService")
public class ActualValuesServiceImpl extends ServiceImpl<ActualValuesMapper, ActualValues>  implements ActualValuesService {

    @Override
    public int deleteByTaskId(long taskId) {
        return baseMapper.delete(new QueryWrapper<ActualValues>().eq("task_id",taskId));
    }

}
