package io.datavines.server.coordinator.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.core.utils.BeanConvertUtils;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.notification.api.entity.SlasSenderMessage;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.server.coordinator.repository.entity.SlasNotification;
import io.datavines.server.coordinator.repository.entity.SlasSender;
import io.datavines.server.coordinator.repository.mapper.SlasNotificationMapper;
import io.datavines.server.coordinator.repository.service.SlasNotificationService;
import io.datavines.server.coordinator.repository.service.SlasReceiverService;
import io.datavines.server.coordinator.repository.service.SlasSenderService;
import io.datavines.spi.PluginLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SlasNotificationServiceImpl extends ServiceImpl<SlasNotificationMapper, SlasNotification> implements SlasNotificationService {

    @Autowired
    private SlasSenderService slasSenderService;

    @Autowired
    private SlasReceiverService slasReceiverService;

    @Autowired
    private SlasNotificationMapper slasNotificationMapper;

    /**
     * get slas sender and receiver configuration from db by slasId. it will return empty Map if slas not association with sender and receiver
     * @param slasId
     * @return
     */
    @Override
    public Map<SlasSenderMessage, Set<SlasReceiverMessage>> getSlasNotificationConfigurationBySlasId(Long slasId){
        //get all notification config
        LambdaQueryWrapper<SlasNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlasNotification::getSlasId, slasId);
        List<SlasNotification> slasNotificationList = list(wrapper);
        if (CollectionUtils.isEmpty(slasNotificationList)){
            return new HashMap<>();
        }
        //make receiverSet and senderSet
        Set<Long> senderSet = slasNotificationList.stream().map(SlasNotification::getSenderId).collect(Collectors.toSet());
        //make notificationSet;
        Set<SlasReceiverMessage> slasReceiverMessages = listReceiverMessageBySlasId(slasId);
        Map<Long, SlasReceiverMessage> receiverMessageMap = slasReceiverMessages
                .stream()
                .collect(Collectors.toMap(SlasReceiverMessage::getId, x -> x));

        List<SlasSender> slasSenders = slasSenderService.listByIds(senderSet);

        Map<Long, SlasSenderMessage> senderMap = slasSenders
                .stream()
                .map(x -> BeanConvertUtils.convertBean(x, SlasSenderMessage::new))
                .collect(Collectors.toMap(SlasSenderMessage::getId, x -> x));
        HashMap<SlasSenderMessage, Set<SlasReceiverMessage>> result = new HashMap<>();
        for(SlasNotification entity: slasNotificationList){
            Long senderId = entity.getSenderId();
            Long receiverId = entity.getReceiverId();
            SlasSenderMessage slasSender = senderMap.get(senderId);
            SlasReceiverMessage slasReceiverMessage = receiverMessageMap.get(receiverId);
            Set<SlasReceiverMessage> existSet = result.getOrDefault(slasSender, new HashSet<>());
            existSet.add(slasReceiverMessage);
            result.putIfAbsent(slasSender, existSet);
        }
        return result;
    }

    public Set<SlasReceiverMessage> listReceiverMessageBySlasId(Long id){
        return slasNotificationMapper.listReceiverMessageBySlasId(id);
    }



    @Override
    public String getConfigJson(String type) {
        return PluginLoader
                .getPluginLoader(SlasHandlerPlugin.class)
                .getOrCreatePlugin(type)
                .getConfigJson();
    }
}
