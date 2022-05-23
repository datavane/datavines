package io.datavines.notification.api.spi;

import io.datavines.notification.api.entity.SlasMessage;
import io.datavines.notification.api.entity.SlasResult;
import io.datavines.spi.SPI;


@SPI
public interface SlasHandlerPlugin {
    /**
     * save message to db then send message to receiver , return the status finally
     * @param slasMessage issue message a
     * @return send status
     */
    SlasResult notify(SlasMessage slasMessage);

    String getConfigJson();
}
