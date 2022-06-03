package io.datavines.notification.api.spi;

import io.datavines.notification.api.entity.SlasNotificationMessage;
import io.datavines.notification.api.entity.SlasNotificationResult;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.notification.api.entity.SlasSenderMessage;
import io.datavines.spi.SPI;

import java.util.Map;
import java.util.Set;


@SPI
public interface SlasHandlerPlugin {
    /**
     * save message to db then send message to receiver , return the status finally
     * @param slasNotificationMessage issue message a
     * @return send status
     */
    SlasNotificationResult notify(SlasNotificationMessage slasNotificationMessage, Map<SlasSenderMessage, Set<SlasReceiverMessage>> config);

    String getConfigSenderJson();

    String getConfigReceiverJson();
}
