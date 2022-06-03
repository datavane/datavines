package io.datavines.server.coordinator.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.datavines.core.utils.BeanConvertUtils;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.notification.api.entity.SlasSenderMessage;
import io.datavines.server.coordinator.repository.entity.SlasNotification;
import io.datavines.server.coordinator.repository.entity.SlasReceiver;
import io.datavines.server.coordinator.repository.entity.SlasSender;
import io.datavines.server.coordinator.repository.mapper.SlasNotificationMapper;
import io.datavines.server.coordinator.repository.service.SlasNotificationService;
import io.datavines.server.coordinator.repository.service.SlasReceiverService;
import io.datavines.server.coordinator.repository.service.SlasSenderService;
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

    /**
     * get slas sender and receiver configuration from db by slasId. it will return empty Map if slas not association with sender and receiver
     * @param slasId
     * @return
     */
    public Map<SlasSenderMessage, Set<SlasReceiverMessage>> getSlasNotificationConfigurationBySlasId(Long slasId){
        LambdaQueryWrapper<SlasNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlasNotification::getSlasId, slasId);
        List<SlasNotification> slasNotificationList = list(wrapper);
        if (CollectionUtils.isEmpty(slasNotificationList)){
            return new HashMap<>();
        }
        Set<Long> receiverSet = slasNotificationList.stream().map(SlasNotification::getReceiverId).collect(Collectors.toSet());
        Set<Long> senderSet = slasNotificationList.stream().map(SlasNotification::getSenderId).collect(Collectors.toSet());
        List<SlasReceiver> receiverList = slasReceiverService.listByIds(receiverSet);
        List<SlasSender> slasSenders = slasSenderService.listByIds(senderSet);
        Map<Long, SlasReceiverMessage> receiverMap = receiverList
                .stream()
                .map(x-> BeanConvertUtils.convertBean(x, SlasReceiverMessage::new))
                .collect(Collectors.toMap(SlasReceiverMessage::getId, x -> x));
        Map<Long, SlasSenderMessage> senderMap = slasSenders
                .stream()
                .map(x -> BeanConvertUtils.convertBean(x, SlasSenderMessage::new))
                .collect(Collectors.toMap(SlasSenderMessage::getId, x -> x));
        HashMap<SlasSenderMessage, Set<SlasReceiverMessage>> result = new HashMap<>();
        for(SlasNotification entity: slasNotificationList){
            Long senderId = entity.getSenderId();
            Long receiverId = entity.getReceiverId();
            SlasSenderMessage slasSender = senderMap.get(senderId);
            SlasReceiverMessage receiver = receiverMap.get(receiverId);
            Set<SlasReceiverMessage> existSet = result.getOrDefault(slasSender, new HashSet<>());
            existSet.add(receiver);
            result.putIfAbsent(slasSender, existSet);
        }
        return result;
    }
}
