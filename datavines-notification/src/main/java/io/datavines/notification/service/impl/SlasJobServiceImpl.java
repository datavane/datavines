package io.datavines.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.notification.entity.SlasJob;
import io.datavines.notification.mapper.SlasJobMapper;
import io.datavines.notification.service.SlasJobService;
import org.springframework.stereotype.Service;

@Service
public class SlasJobServiceImpl extends ServiceImpl<SlasJobMapper, SlasJob> implements SlasJobService {
}
