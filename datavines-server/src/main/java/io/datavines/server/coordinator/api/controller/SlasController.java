package io.datavines.server.coordinator.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.datavines.common.exception.DataVinesException;
import io.datavines.core.aop.RefreshToken;
import io.datavines.core.constant.DataVinesConstants;
import io.datavines.core.enums.ApiStatus;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.core.utils.BeanConvertUtils;
import io.datavines.notification.api.entity.SlasNotificationMessage;
import io.datavines.notification.api.entity.SlasNotificationResult;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.notification.api.entity.SlasSenderMessage;
import io.datavines.notification.core.NotificationManager;
import io.datavines.notification.core.client.NotificationClient;
import io.datavines.server.coordinator.api.dto.bo.*;
import io.datavines.server.coordinator.api.dto.vo.SlasVo;
import io.datavines.server.coordinator.repository.entity.Slas;
import io.datavines.server.coordinator.repository.entity.SlasNotification;
import io.datavines.server.coordinator.repository.entity.SlasReceiver;
import io.datavines.server.coordinator.repository.entity.SlasSender;
import io.datavines.server.coordinator.repository.service.SlasNotificationService;
import io.datavines.server.coordinator.repository.service.SlasReceiverService;
import io.datavines.server.coordinator.repository.service.SlasSenderService;
import io.datavines.server.coordinator.repository.service.SlasService;
import io.datavines.server.utils.ContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@Api(value = "slas", tags = "slas", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/slas", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
@Validated
public class SlasController {

    @Autowired
    private SlasService slasService;

    @Autowired
    private SlasSenderService slasSenderService;

    @Autowired
    private SlasReceiverService slasReceiverService;

    @Autowired
    private SlasNotificationService slasNotificationService;

    @Autowired
    private NotificationClient client;

    @ApiOperation(value = "test slas")
    @GetMapping(value = "/test")
    public Object test(){
        SlasNotificationMessage message = new SlasNotificationMessage();
        message.setMessage("test");
        message.setSubject("test");
        SlasSender byId = slasSenderService.getById(1);
        SlasSenderMessage senderMessage = BeanConvertUtils.convertBean(byId, SlasSenderMessage::new);
        Map<SlasSenderMessage, Set<SlasReceiverMessage>> map = new HashMap<>();
        SlasReceiver receiver = slasReceiverService.getById(1);
        SlasReceiverMessage slasReceiver = BeanConvertUtils.convertBean(receiver, SlasReceiverMessage::new);
        HashSet<SlasReceiverMessage> set = new HashSet<>();
        set.add(slasReceiver);
        map.put(senderMessage, set);
        SlasNotificationResult notify = client.notify(message, map);
        return notify;
    }


    @ApiOperation(value = "list slas")
    @GetMapping(value = "/list/{workSpaceId}")
    public Object listSlas(@PathVariable("workSpaceId") Long workSpaceId){
        List<SlasVo> slasVoList = slasService.listSlas(workSpaceId);
        return slasVoList;
    }

    @ApiOperation(value = "create slas")
    @PostMapping( consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createSlas(@Valid @RequestBody SlasCreate create){
        String name = create.getName();
        LambdaQueryWrapper<Slas> wrapper = new LambdaQueryWrapper();
        wrapper.eq(Slas::getWorkSpaceId, create.getWorkSpaceId());
        wrapper.eq(Slas::getName, name);
        Slas existSlas = slasService.getOne(wrapper);
        if (Objects.nonNull(existSlas)){
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, name);
        }
        Slas slas = BeanConvertUtils.convertBean(create, Slas::new);
        slas.setCreateBy(ContextHolder.getUserId());
        LocalDateTime now = LocalDateTime.now();
        slas.setUpdateBy(ContextHolder.getUserId());
        slas.setUpdateTime(now);
        slas.setCreateTime(now);
        boolean success = slasService.save(slas);
        if (!success){
            throw new DataVinesException("create slas error");
        }
        return slas;
    }

    @ApiOperation(value = "update slas")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object update(@Valid @RequestBody SlasUpdate update){
        LambdaQueryWrapper<Slas> wrapper = new LambdaQueryWrapper();
        wrapper.eq(Slas::getWorkSpaceId, update.getWorkSpaceId());
        wrapper.eq(Slas::getName, update.getName());
        Slas existSlas = slasService.getOne(wrapper);
        if (Objects.nonNull(existSlas) && !existSlas.getId().equals(update.getId())){
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, update.getName());
        }
        Slas slas = BeanConvertUtils.convertBean(update, Slas::new);
        slas.setUpdateTime(LocalDateTime.now());
        boolean save = slasService.updateById(slas);
        return save;
    }

    @ApiOperation(value = "delete slas")
    @DeleteMapping(value = "/{id}")
    public Object deleteSlas(@PathVariable("id") Long id){
        boolean remove = slasService.deleteById(id);
        return remove;
    }

    @ApiOperation(value = "get support plugin")
    @GetMapping(value = "/plugin/support")
    public Object getSupportPlugin(){
        Set<String> supportPlugin = slasService.getSupportPlugin();
        return supportPlugin;
    }

    @ApiOperation(value = "get config param of sender")
    @GetMapping(value = "/sender/config/{type}")
    public Object getSenderConfigJson(@PathVariable("type") String type){
        String json = slasService.getSenderConfigJson(type);
        return json;
    }

    @ApiOperation(value = "get config param of sender")
    @GetMapping(value = "/config/{type}")
    public Object getConfigJson(@PathVariable("type") String type){
        String json = slasNotificationService.getConfigJson(type);
        return json;
    }

    @ApiOperation(value = "get config param of receiver")
    @GetMapping(value = "/receiver/config/{type}")
    public Object getReceiverConfigJson(@PathVariable("type") String type){
        String json = slasService.getReceiverConfigJson(type);
        return json;
    }

    @ApiOperation(value = "list sender")
    @GetMapping(value = "/sender/list/{workSpaceId}")
    public Object listSenders(@PathVariable("workSpaceId") Long workSpaceId){
        LambdaQueryWrapper<SlasSender> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SlasSender::getWorkSpaceId, workSpaceId);
        List<SlasSender> list = slasSenderService.list(wrapper);
        return list;
    }

    @ApiOperation(value = "create sender")
    @PostMapping(value = "/sender",consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createSender(@Valid @RequestBody SlasSenderCreate create){
        SlasSender slasSender = BeanConvertUtils.convertBean(create, SlasSender::new);
        LocalDateTime now = LocalDateTime.now();
        slasSender.setCreateTime(now);
        slasSender.setUpdateTime(now);
        boolean success = slasSenderService.save(slasSender);
        if (!success){
            throw new DataVinesException("create sender error");
        }
        return slasSender;
    }


    @ApiOperation(value = "update sender")
    @PutMapping(value = "/sender",consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateSender(@Valid @RequestBody SlasSenderUpdate update){
        LambdaQueryWrapper<SlasSender> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SlasSender::getWorkSpaceId, update.getWorkSpaceId());
        wrapper.eq(SlasSender::getName, update.getName());
        SlasSender existSlas = slasSenderService.getOne(wrapper);
        if (Objects.nonNull(existSlas) && !existSlas.getId().equals(update.getId())){
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, update.getName());
        }
        SlasSender sender = BeanConvertUtils.convertBean(update, SlasSender::new);
        sender.setUpdateTime(LocalDateTime.now());
        boolean save = slasSenderService.updateById(sender);
        return save;
    }

    @ApiOperation(value = "delete sender")
    @DeleteMapping(value = "/sender/{id}")
    public Object deleteSender(@PathVariable("id") Long id){
        boolean remove = slasSenderService.removeById(id);
        return remove;
    }

    @ApiOperation(value = "list receiver")
    @GetMapping(value = "/receiver/list/{workSpaceId}")
    public Object listReceiver(@PathVariable("workSpaceId") Long workSpaceId){
        LambdaQueryWrapper<SlasReceiver> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SlasReceiver::getWorkSpaceId, workSpaceId);
        List<SlasReceiver> list = slasReceiverService.list(wrapper);
        return list;
    }


    @ApiOperation(value = "create receiver")
    @PostMapping(value = "/receiver")
    public Object createReceiver(@RequestBody SlasReceiverCreate create){
        SlasReceiver slasReceiver = BeanConvertUtils.convertBean(create, SlasReceiver::new);
        LocalDateTime now = LocalDateTime.now();
        slasReceiver.setCreateTime(now);
        slasReceiver.setUpdateTime(now);
        boolean success = slasReceiverService.save(slasReceiver);
        if (!success){
            throw new DataVinesException("create receiver error");
        }
        return slasReceiver;

    }

    @ApiOperation(value = "update receiver")
    @PutMapping(value = "/receiver")
    public Object updateReceiver(@RequestBody SlasReceiverUpdate update){
        SlasReceiver slasReceiver = BeanConvertUtils.convertBean(update, SlasReceiver::new);
        slasReceiver.setUpdateTime(LocalDateTime.now());
        return slasReceiverService.saveOrUpdate(slasReceiver);
    }


    @ApiOperation(value = "delete receiver")
    @DeleteMapping(value = "/receiver/{id}")
    public Object deleteReceiver(@PathVariable("id") Long id){
        boolean remove = slasReceiverService.removeById(id);
        return remove;
    }

    @ApiOperation(value = "create notification")
    @PostMapping(value = "/notification")
    public Object createNotification(@RequestBody SlasNotificationCreate create){
        SlasNotification bean = BeanConvertUtils.convertBean(create, SlasNotification::new);
        bean.setCreateBy(ContextHolder.getUserId());
        LocalDateTime now = LocalDateTime.now();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        bean.setUpdateBy(ContextHolder.getUserId());
        boolean success = slasNotificationService.save(bean);
        if (!success){
            throw new DataVinesException("create sender error");
        }
        return bean;
    }
    @ApiOperation(value = "delete notification")
    @DeleteMapping(value = "/notification/{id}")
    public Object deleteNotification(@PathVariable("id") Long id){
        boolean remove = slasNotificationService.removeById(id);
        return remove;
    }



}
