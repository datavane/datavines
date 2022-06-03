package io.datavines.server.coordinator.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.server.coordinator.repository.entity.SlasReceiver;
import io.datavines.server.coordinator.repository.mapper.SlasReceiverMapper;
import io.datavines.server.coordinator.repository.service.SlasReceiverService;
import org.springframework.stereotype.Service;

@Service
public class SlasReceiverServiceImpl extends ServiceImpl<SlasReceiverMapper, SlasReceiver> implements SlasReceiverService {
}
