package io.datavines.server.coordinator.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.coordinator.repository.entity.SlasSender;
import io.datavines.server.coordinator.repository.mapper.SlasSenderMapper;
import io.datavines.server.coordinator.repository.service.SlasSenderService;
import org.springframework.stereotype.Service;

@Service
public class SlasSenderServiceImpl extends ServiceImpl<SlasSenderMapper, SlasSender> implements SlasSenderService {
}
