package io.datavines.server.coordinator.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.server.coordinator.repository.entity.SlasNotification;

public interface SlasNotificationService extends IService<SlasNotification>{
    String getConfigJson(String type);
}
