package io.datavines.server.coordinator.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.coordinator.repository.entity.SlasJob;
import io.datavines.server.coordinator.repository.mapper.SlasJobMapper;
import io.datavines.server.coordinator.repository.service.SlasJobService;
import org.springframework.stereotype.Service;

@Service
public class SlasJobServiceImpl extends ServiceImpl<SlasJobMapper, SlasJob> implements SlasJobService {
}
