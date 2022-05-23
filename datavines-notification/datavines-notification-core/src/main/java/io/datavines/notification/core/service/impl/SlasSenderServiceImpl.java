package io.datavines.notification.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.notification.core.entity.SlasSender;
import io.datavines.notification.core.mapper.SlasSenderMapper;
import io.datavines.notification.core.service.SlasSenderService;
import org.springframework.stereotype.Service;

@Service
public class SlasSenderServiceImpl extends ServiceImpl<SlasSenderMapper, SlasSender> implements SlasSenderService {
}
