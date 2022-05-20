package io.datavines.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.notification.dto.vo.SlasVo;
import io.datavines.notification.entity.Slas;
import io.datavines.notification.mapper.SlasMapper;
import io.datavines.notification.service.SlasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SlasServiceImpl extends ServiceImpl<SlasMapper, Slas> implements SlasService {

    @Autowired
    private SlasMapper slasMapper;

    @Override
    public List<SlasVo> listSlas(Long workSpaceId) {
        List<SlasVo> res = slasMapper.listSlas(workSpaceId);
        return res;
    }
}
