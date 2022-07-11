package io.datavines.storage.plugin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datavines.common.CommonConstants;
import io.datavines.common.param.form.PluginParams;
import io.datavines.common.param.form.PropsType;
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.props.InputParamsProps;
import io.datavines.common.param.form.type.InputParam;
import io.datavines.storage.api.StorageConnector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MysqlStorageConnector implements StorageConnector {

    @Override
    public String getConfigJson() {
        InputParam host = getInputParam("host", "地址", "请填入连接地址", 1, Validate.newBuilder()
                .setRequired(true).setMessage("请填入连接地址")
                .build());
        InputParam port = getInputParam("port", "端口", "请填入端口号", 1, Validate.newBuilder()
                .setRequired(true).setMessage("请填入端口号")
                .build());
        InputParam database = getInputParam("database", "数据库", "请填入数据库", 1, Validate.newBuilder()
                .setRequired(true).setMessage("请填入数据库")
                .build());
        InputParam user = getInputParam("user", "用户名", "请填入用户名", 1, Validate.newBuilder()
                .setRequired(true).setMessage("请填入用户名")
                .build());
        InputParam password = getInputParam("password", "密码", "请填入密码", 1, Validate.newBuilder()
                .setRequired(true).setMessage("请填入密码")
                .build());
        InputParam properties = getInputParamNoValidate("properties", "参数", "请填入参数，格式为key=value&key1=value1", 2);

        List<PluginParams> params = new ArrayList<>();
        params.add(host);
        params.add(port);
        params.add(database);
        params.add(user);
        params.add(password);
        params.add(properties);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("json parse error : {}", e.getMessage(), e);
        }

        return result;
    }

    private InputParam getInputParam(String field, String title, String placeholder, int rows, Validate validate) {
        return InputParam
                .newBuilder(field, title)
                .addValidate(validate)
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXT)
                .setRows(rows)
                .setPlaceholder(placeholder)
                .setEmit(null)
                .build();
    }

    private InputParam getInputParamNoValidate(String field, String title, String placeholder, int rows) {
        return InputParam
                .newBuilder(field, title)
                .setProps(new InputParamsProps().setDisabled(false))
                .setSize(CommonConstants.SMALL)
                .setType(PropsType.TEXTAREA)
                .setRows(rows)
                .setPlaceholder(placeholder)
                .setEmit(null)
                .build();
    }
}
