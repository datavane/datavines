package io.datavines.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.notification.entity.Slas;
import io.datavines.notification.mapper.SlasMapper;
import io.datavines.notification.service.SlasService;
import org.springframework.stereotype.Service;

@Service
public class SlasServiceImpl extends ServiceImpl<SlasMapper, Slas> implements SlasService {
}
