package io.datavines.server.coordinator.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.notification.api.entity.SlasSenderMessage;
import io.datavines.server.coordinator.repository.entity.SlasNotification;

import java.util.Map;
import java.util.Set;

public interface SlasNotificationService extends IService<SlasNotification>{
    Map<SlasSenderMessage, Set<SlasReceiverMessage>> getSlasNotificationConfigurationBySlasId(Long slasId);

    String getConfigJson(String type);
}
