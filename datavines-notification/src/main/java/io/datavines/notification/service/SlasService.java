package io.datavines.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.notification.dto.vo.SlasVo;
import io.datavines.notification.entity.Slas;

import java.util.List;

public interface SlasService extends IService<Slas> {
    List<SlasVo> listSlas(Long workSpaceId);
}
