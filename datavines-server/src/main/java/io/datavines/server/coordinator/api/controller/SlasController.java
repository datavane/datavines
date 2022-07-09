/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.server.coordinator.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.datavines.common.exception.DataVinesException;
import io.datavines.core.aop.RefreshToken;
import io.datavines.core.constant.DataVinesConstants;
import io.datavines.core.enums.ApiStatus;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.core.utils.BeanConvertUtils;
import io.datavines.notification.api.entity.SlaNotificationMessage;
import io.datavines.notification.api.entity.SlaNotificationResult;
import io.datavines.notification.api.entity.SlaConfigMessage;
import io.datavines.notification.api.entity.SlaSenderMessage;
import io.datavines.notification.core.client.NotificationClient;
import io.datavines.server.coordinator.api.dto.bo.*;
import io.datavines.server.coordinator.api.dto.vo.SlaJobVo;
import io.datavines.server.coordinator.api.dto.vo.SlaSenderVo;
import io.datavines.server.coordinator.api.dto.vo.SlaVo;
import io.datavines.server.coordinator.repository.entity.Sla;
import io.datavines.server.coordinator.repository.entity.SlaJob;
import io.datavines.server.coordinator.repository.entity.SlaNotification;
import io.datavines.server.coordinator.repository.entity.SlaSender;
import io.datavines.server.coordinator.repository.service.SlaNotificationService;
import io.datavines.server.coordinator.repository.service.SlaSenderService;
import io.datavines.server.coordinator.repository.service.SlaService;
import io.datavines.server.coordinator.repository.service.SlaJobService;
import io.datavines.server.utils.ContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@Api(value = "sla", tags = "sla", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/sla", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
@Validated
@Slf4j
public class SlasController {

    @Autowired
    private SlaService slaService;

    @Autowired
    private SlaSenderService slaSenderService;

    @Autowired
    private SlaNotificationService slaNotificationService;

    @Autowired
    private NotificationClient client;

    @Autowired
    private SlaJobService slaJobService;


    @ApiOperation(value = "list job")
    @GetMapping(value = "/job/list")
    public Object listSlaJob(@RequestParam("slaId") Long id){
        List<SlaJobVo> list = slaJobService.listSlaJob(id);
        return list;
    }

    @ApiOperation(value = "create sla job")
    @PostMapping(value = "/job", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createSlaJob(@Valid @RequestBody SlaJobCreate create){
        LambdaQueryWrapper<SlaJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlaJob::getSlaId,create.getSlaId());
        wrapper.eq(SlaJob::getJobId,create.getJobId());
        SlaJob one = slaJobService.getOne(wrapper);
        if (Objects.nonNull(one)){
            log.info("SlaJob has been create {}", create);
            throw new DataVinesException("SlaJob has been create");
        }
        SlaJob slaJob = new SlaJob();
        slaJob.setSlaId(create.getSlaId());
        slaJob.setJobId(create.getJobId());
        slaJob.setCreateBy(ContextHolder.getUserId());
        slaJob.setUpdateBy(ContextHolder.getUserId());
        return slaJobService.save(slaJob);
    }

    @ApiOperation(value = "update sla job")
    @PutMapping(value = "/job", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateSlaJob(@Valid @RequestBody SlaJobUpdate update){
        LambdaQueryWrapper<SlaJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlaJob::getSlaId,update.getSlaId());
        wrapper.eq(SlaJob::getJobId,update.getJobId());
        SlaJob one = slaJobService.getOne(wrapper);
        if (Objects.nonNull(one) && !one.getId().equals(update.getId())){
            log.info("db has slajob {} is same as update slajob {}", one, update);
            throw new DataVinesException("SlaJob has been exist");
        }
        SlaJob slaJob = new SlaJob();
        slaJob.setSlaId(update.getSlaId());
        slaJob.setJobId(update.getJobId());
        slaJob.setId(update.getId());
        slaJob.setUpdateTime(LocalDateTime.now());
        slaJob.setUpdateBy(ContextHolder.getUserId());
        return slaJobService.save(slaJob);
    }

    @ApiOperation(value = "create sla job")
    @DeleteMapping(value = "/job/{slaJobId}")
    public Object deleteSlaJob(@PathVariable("slaJobId") Long  slaJobId){
        return slaJobService.removeById(slaJobId);
    }


    @ApiOperation(value = "test sla")
    @GetMapping(value = "/test/{slaId}")
    public Object test(@PathVariable("slaId") Long slaId){
        SlaNotificationMessage message = new SlaNotificationMessage();
        message.setMessage("test");
        message.setSubject("just test slaId");
        Map<SlaSenderMessage, Set<SlaConfigMessage>> configuration = slaNotificationService.getSlasNotificationConfigurationBySlasId(slaId);
        SlaNotificationResult notify = client.notify(message, configuration);
        return notify;
    }


    @ApiOperation(value = "page list slas")
    @GetMapping(value = "/page")
    public Object listSlas(@RequestParam("workSpaceId") Long workSpaceId,
                           @RequestParam(value = "searchVal", required = false) String searchVal,
                           @RequestParam("pageNumber") Integer pageNumber,
                           @RequestParam("pageSize") Integer pageSize){
        IPage<SlaVo> slaVoList = slaService.listSlas(workSpaceId, searchVal, pageNumber, pageSize);
        return slaVoList;
    }

    @ApiOperation(value = "create sla")
    @PostMapping( consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createSla(@Valid @RequestBody SlaCreate create){
        String name = create.getName();
        LambdaQueryWrapper<Sla> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sla::getWorkSpaceId, create.getWorkSpaceId());
        wrapper.eq(Sla::getName, name);
        Sla existSla = slaService.getOne(wrapper);
        if (Objects.nonNull(existSla)){
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, name);
        }
        Sla sla = BeanConvertUtils.convertBean(create, Sla::new);
        sla.setCreateBy(ContextHolder.getUserId());
        LocalDateTime now = LocalDateTime.now();
        sla.setUpdateBy(ContextHolder.getUserId());
        sla.setUpdateTime(now);
        sla.setCreateTime(now);
        boolean success = slaService.save(sla);
        if (!success){
            throw new DataVinesException("create slas error");
        }
        return sla;
    }

    @ApiOperation(value = "update slas")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object update(@Valid @RequestBody SlaUpdate update){
        LambdaQueryWrapper<Sla> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sla::getWorkSpaceId, update.getWorkSpaceId());
        wrapper.eq(Sla::getName, update.getName());
        Sla existSla = slaService.getOne(wrapper);
        if (Objects.nonNull(existSla) && !existSla.getId().equals(update.getId())){
            log.info("db has sla {} is same as update {}", existSla, update);
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, update.getName());
        }
        Sla sla = BeanConvertUtils.convertBean(update, Sla::new);
        sla.setUpdateBy(ContextHolder.getUserId());
        sla.setUpdateTime(LocalDateTime.now());
        boolean save = slaService.updateById(sla);
        return save;
    }

    @ApiOperation(value = "delete sla")
    @DeleteMapping(value = "/{id}")
    public Object deleteSla(@PathVariable("id") Long id){
        boolean remove = slaService.deleteById(id);
        return remove;
    }

    @ApiOperation(value = "get support plugin")
    @GetMapping(value = "/plugin/support")
    public Object getSupportPlugin(){
        Set<String> supportPlugin = slaService.getSupportPlugin();
        return supportPlugin;
    }

    @ApiOperation(value = "get config param of sender")
    @GetMapping(value = "/sender/config/{type}")
    public Object getSenderConfigJson(@PathVariable("type") String type){
        String json = slaService.getSenderConfigJson(type);
        return json;
    }

    @ApiOperation(value = "get config param of notification")
    @GetMapping(value = "/notification/config/{type}")
    public Object getNotificationConfigJson(@PathVariable("type") String type){
        String json = slaNotificationService.getConfigJson(type);
        return json;
    }



    @ApiOperation(value = "page list sender")
    @GetMapping(value = "/sender/page")
    public Object listSenders(@RequestParam("workSpaceId") Long workSpaceId,
                              @RequestParam(value = "searchVal", required = false) String searchVal,
                              @RequestParam("pageNumber") Integer pageNumber,
                              @RequestParam("pageSize") Integer pageSize){
        IPage<SlaSenderVo> result = slaSenderService.pageListSender(workSpaceId, searchVal, pageNumber, pageSize);
        return result;
    }

    @ApiOperation(value = " list sender")
    @GetMapping(value = "/sender/list")
    public Object listSenders(@RequestParam("workSpaceId") Long workSpaceId,
                              @RequestParam(value = "type") String type,
                              @RequestParam(value = "searchVal", required = false) String searchVal){
        List<SlaSenderVo> result = slaSenderService.listSenders(workSpaceId, searchVal, type);
        return result;
    }

    @ApiOperation(value = "create sender")
    @PostMapping(value = "/sender",consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createSender(@Valid @RequestBody SlaSenderCreate create){
        SlaSender slaSender = BeanConvertUtils.convertBean(create, SlaSender::new);
        LocalDateTime now = LocalDateTime.now();
        slaSender.setCreateTime(now);
        slaSender.setCreateBy(ContextHolder.getUserId());
        slaSender.setUpdateBy(ContextHolder.getUserId());
        slaSender.setUpdateTime(now);
        boolean success = slaSenderService.save(slaSender);
        if (!success){
            throw new DataVinesException("create sender error");
        }
        return slaSender;
    }


    @ApiOperation(value = "update sender")
    @PutMapping(value = "/sender",consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateSender(@Valid @RequestBody SlaSenderUpdate update){
        LambdaQueryWrapper<SlaSender> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SlaSender::getWorkSpaceId, update.getWorkSpaceId());
        wrapper.eq(SlaSender::getName, update.getName());
        SlaSender existSlas = slaSenderService.getOne(wrapper);
        if (Objects.nonNull(existSlas) && !existSlas.getId().equals(update.getId())){
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, update.getName());
        }
        SlaSender sender = BeanConvertUtils.convertBean(update, SlaSender::new);
        sender.setUpdateTime(LocalDateTime.now());
        boolean save = slaSenderService.updateById(sender);
        return save;
    }

    @ApiOperation(value = "delete sender")
    @DeleteMapping(value = "/sender/{id}")
    public Object deleteSender(@PathVariable("id") Long id){
        boolean remove = slaSenderService.removeById(id);
        return remove;
    }


    @ApiOperation(value = "create notification")
    @PostMapping(value = "/notification")
    public Object createNotification(@RequestBody SlaNotificationCreate create){
        SlaNotification bean = BeanConvertUtils.convertBean(create, SlaNotification::new);
        bean.setCreateBy(ContextHolder.getUserId());
        LocalDateTime now = LocalDateTime.now();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        bean.setUpdateBy(ContextHolder.getUserId());
        boolean success = slaNotificationService.save(bean);
        if (!success){
            throw new DataVinesException("create sender error");
        }
        return bean;
    }
    @ApiOperation(value = "update notification")
    @PutMapping(value = "/notification")
    public Object updateNotification(@RequestBody SlaNotificationUpdate update){
        SlaNotification bean = BeanConvertUtils.convertBean(update, SlaNotification::new);
        bean.setCreateBy(ContextHolder.getUserId());
        LocalDateTime now = LocalDateTime.now();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        bean.setUpdateBy(ContextHolder.getUserId());
        boolean success = slaNotificationService.save(bean);
        if (!success){
            throw new DataVinesException("update sender error");
        }
        return bean;
    }
    @ApiOperation(value = "delete notification")
    @DeleteMapping(value = "/notification/{id}")
    public Object deleteNotification(@PathVariable("id") Long id){
        boolean remove = slaNotificationService.removeById(id);
        return remove;
    }

    @ApiOperation(value = "page list notification")
    @GetMapping("/notification/page")
    public Object pageListNotification(@RequestParam("workSpaceId") Long workSpaceId,
                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                   @RequestParam("pageNumber") Integer pageNumber,
                                   @RequestParam("pageSize") Integer pageSize){
        LambdaQueryWrapper<SlaNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlaNotification::getWorkSpaceId, workSpaceId);
        Page<SlaNotification> page = new Page<>(pageNumber, pageSize);
        IPage<SlaNotification> result = slaNotificationService.pageListNotification(page, workSpaceId, searchVal);
        return result;
    }


}
