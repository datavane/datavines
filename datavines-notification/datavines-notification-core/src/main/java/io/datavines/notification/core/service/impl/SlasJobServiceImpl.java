package io.datavines.notification.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.notification.core.entity.SlasJob;
import io.datavines.notification.core.mapper.SlasJobMapper;
import io.datavines.notification.core.service.SlasJobService;
import org.springframework.stereotype.Service;

@Service
public class SlasJobServiceImpl extends ServiceImpl<SlasJobMapper, SlasJob> implements SlasJobService {
}
