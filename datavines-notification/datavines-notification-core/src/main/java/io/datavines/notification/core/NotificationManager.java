package io.datavines.notification.core;

import io.datavines.common.exception.DataVinesException;
import io.datavines.notification.api.entity.SlasNotificationMessage;
import io.datavines.notification.api.entity.SlasNotificationResult;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.notification.api.entity.SlasSenderMessage;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class NotificationManager {

    private Set<String> supportedPlugins;

    public NotificationManager(){
        supportedPlugins = PluginLoader
                .getPluginLoader(SlasHandlerPlugin.class)
                .getSupportedPlugins();
    }


    public SlasNotificationResult notify(SlasNotificationMessage slasNotificationMessage, Map<SlasSenderMessage, Set<SlasReceiverMessage>> config){
        if (config == null || config.isEmpty()){
            throw new DataVinesException("message cannot be send without sender and receiver");
        }
        SlasNotificationResult result = new SlasNotificationResult();
        result.setStatus(true);

        for(Map.Entry<SlasSenderMessage, Set<SlasReceiverMessage>> entry: config.entrySet()){
            String type = entry.getKey().getType();
            if (supportedPlugins.contains(type)){
                throw new DataVinesException("sender type not support of "+ type);
            }
            SlasHandlerPlugin handlerPlugin = PluginLoader
                    .getPluginLoader(SlasHandlerPlugin.class)
                    .getOrCreatePlugin(type);
            Map<SlasSenderMessage, Set<SlasReceiverMessage>> senderEntity = new HashMap(){
                {
                    put(entry.getKey(), entry.getValue());
                }
            };
            SlasNotificationResult entryResult = handlerPlugin.notify(slasNotificationMessage, senderEntity);
            result.merge(entryResult);
        }
        return result;
    }

}
