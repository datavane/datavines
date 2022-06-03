package io.datavines.server.coordinator.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.datavines.core.aop.RefreshToken;
import io.datavines.core.constant.DataVinesConstants;
import io.datavines.core.enums.ApiStatus;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.core.utils.BeanConvertUtils;
import io.datavines.server.coordinator.api.dto.bo.SlasCreate;
import io.datavines.server.coordinator.api.dto.bo.SlasSenderCreate;
import io.datavines.server.coordinator.api.dto.bo.SlasUpdate;
import io.datavines.server.coordinator.api.dto.vo.SlasVo;
import io.datavines.server.coordinator.repository.entity.Slas;
import io.datavines.server.coordinator.repository.entity.SlasSender;
import io.datavines.server.coordinator.repository.service.SlasSenderService;
import io.datavines.server.coordinator.repository.service.SlasService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Api(value = "datasource", tags = "datasource", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/slas", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
@Validated
public class SlasController {

    @Autowired
    private SlasService slasService;

    @Autowired
    private SlasSenderService slasSenderService;

    @ApiOperation(value = "list slas")
    @GetMapping(value = "/list/{workSpaceId}")
    public Object listSlas(@PathVariable("workSpaceId") Long workSpaceId){
        List<SlasVo> slasVoList = slasService.listSlas(workSpaceId);
        return slasVoList;
    }

    @ApiOperation(value = "create slas")
    @PostMapping( consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object insert(@Valid @RequestBody SlasCreate create){
        String name = create.getName();
        LambdaQueryWrapper<Slas> wrapper = new LambdaQueryWrapper();
        wrapper.eq(Slas::getWorkSpaceId, create.getWorkSpaceId());
        wrapper.eq(Slas::getName, name);
        Slas existSlas = slasService.getOne(wrapper);
        if (Objects.nonNull(existSlas)){
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, name);
        }
        Slas slas = BeanConvertUtils.convertBean(create, Slas::new);
        boolean save = slasService.save(slas);
        return save;
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
        boolean save = slasService.updateById(slas);
        return save;
    }

    @ApiOperation(value = "delete slas")
    @DeleteMapping(value = "/{id}")
    public Object update(@PathVariable("id") Long id){
        boolean remove = slasService.deleteById(id);
        return remove;
    }

    @ApiOperation(value = "get support plugin")
    @GetMapping(value = "/sender/support")
    public Object getSupportPlugin(){
        Set<String> supportPlugin = slasService.getSupportPlugin();
        return supportPlugin;
    }

    @ApiOperation(value = "get config param")
    @GetMapping(value = "/sender/config/{type}")
    public Object getSenderConfigJson(@PathVariable("type") String type){
        String json = slasService.getSenderConfigJson(type);
        return json;
    }

    @ApiOperation(value = "get config param")
    @GetMapping(value = "/receiver/config/{type}")
    public Object getReceiverConfigJson(@PathVariable("type") String type){
        String json = slasService.getReceiverConfigJson(type);
        return json;
    }

    @ApiOperation(value = "create sender")
    @PostMapping(value = "/sender/list/{workSpaceId}")
    public Object listSenders(@PathVariable("workSpaceId") Long workSpaceId){
        LambdaQueryWrapper<SlasSender> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SlasSender::getWorkSpaceId, workSpaceId);
        List<SlasSender> list = slasSenderService.list(wrapper);
        return list;
    }

    @ApiOperation(value = "create sender")
    @PostMapping(value = "/sender",consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createSender(SlasSenderCreate create){
        String name = create.getName();
        LambdaQueryWrapper<SlasSender> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SlasSender::getWorkSpaceId, create.getWorkSpaceId());
        wrapper.eq(SlasSender::getName, name);
        SlasSender existSlas = slasSenderService.getOne(wrapper);
        if (Objects.nonNull(existSlas)){
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, name);
        }
        SlasSender sender = BeanConvertUtils.convertBean(create, SlasSender::new);
        boolean save = slasSenderService.save(sender);
        return save;
    }

    @ApiOperation(value = "update sender")
    @PutMapping(value = "/sender",consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateSender(@Valid @RequestBody SlasUpdate update){
        LambdaQueryWrapper<SlasSender> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SlasSender::getWorkSpaceId, update.getWorkSpaceId());
        wrapper.eq(SlasSender::getName, update.getName());
        SlasSender existSlas = slasSenderService.getOne(wrapper);
        if (Objects.nonNull(existSlas) && !existSlas.getId().equals(update.getId())){
            throw new DataVinesServerException(ApiStatus.SLAS_ALREADY_EXIST_ERROR, update.getName());
        }
        SlasSender sender = BeanConvertUtils.convertBean(update, SlasSender::new);
        boolean save = slasSenderService.updateById(sender);
        return save;
    }

    @ApiOperation(value = "delete sender")
    @DeleteMapping(value = "/sender/{id}")
    public Object deleteSender(@PathVariable("id") Long id){
        boolean remove = slasSenderService.removeById(id);
        return remove;
    }





}
