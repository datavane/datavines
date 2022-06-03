package io.datavines.server.coordinator.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.server.coordinator.api.dto.vo.SlasVo;
import io.datavines.server.coordinator.repository.entity.Slas;

import java.util.List;
import java.util.Set;

public interface SlasService extends IService<Slas> {

    List<SlasVo> listSlas(Long workSpaceId);

    boolean deleteById(Long id);

    String getSenderConfigJson(String type);
    String getReceiverConfigJson(String type);

    Set<String> getSupportPlugin();
}
