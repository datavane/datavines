package io.datavines.notification.controller;

import io.datavines.notification.dto.vo.SlasVo;
import io.datavines.notification.service.SlasService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "datasource", tags = "datasource", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
//@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/datasource", produces = MediaType.APPLICATION_JSON_VALUE)
//@RefreshToken
public class SlasController {

    @Autowired
    private SlasService slasService;

    @ApiOperation(value = "list slas")
    @PostMapping(value = "/listSlas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object listSlas(Long workSpaceId){
        List<SlasVo> slasVoList = slasService.listSlas(workSpaceId);
        return slasVoList;

    }

}
