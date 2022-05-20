package io.datavines.notification.spi;

import io.datavines.notification.dto.SlasMessage;
import io.datavines.notification.dto.SlasResult;
import io.datavines.spi.SPI;


@SPI
public interface SlasHandlerPlugin {
    /**
     * save message to db then send message to receiver , return the status finally
     * @param slasMessage issue message a
     * @return send status
     */
    SlasResult notify(SlasMessage slasMessage);
}
