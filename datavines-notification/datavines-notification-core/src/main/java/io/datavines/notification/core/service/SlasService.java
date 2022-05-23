package io.datavines.notification.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.notification.core.entity.Slas;
import io.datavines.notification.core.dto.vo.SlasVo;

import java.util.List;

public interface SlasService extends IService<Slas> {

    List<SlasVo> listSlas(Long workSpaceId);

    boolean deleteById(Long id);

    String getConfigJson(String type);
}
