package io.datavines.server.coordinator.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.server.coordinator.repository.entity.SlasNotification;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

@Mapper
public interface SlasNotificationMapper extends BaseMapper<SlasNotification> {
    Set<SlasReceiverMessage> listReceiverMessageBySlasId(Long id);
}
