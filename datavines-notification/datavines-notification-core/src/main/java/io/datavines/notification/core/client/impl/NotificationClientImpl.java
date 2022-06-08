package io.datavines.notification.core.client.impl;

import io.datavines.notification.api.entity.SlasNotificationMessage;
import io.datavines.notification.api.entity.SlasNotificationResult;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.notification.api.entity.SlasSenderMessage;
import io.datavines.notification.core.NotificationManager;
import io.datavines.notification.core.client.NotificationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class NotificationClientImpl implements NotificationClient {

    @Autowired
    private NotificationManager notificationManager;

    @Override
    public SlasNotificationResult notify(SlasNotificationMessage slasNotificationMessage, Map<SlasSenderMessage, Set<SlasReceiverMessage>> config) {
        SlasNotificationResult result = notificationManager.notify(slasNotificationMessage, config);
        return result;
    }
}
