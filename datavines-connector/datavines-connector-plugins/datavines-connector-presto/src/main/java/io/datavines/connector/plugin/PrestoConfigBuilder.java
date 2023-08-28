package io.datavines.connector.plugin;

import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.type.InputParam;

public class PrestoConfigBuilder extends JdbcConfigBuilder {

    @Override
    protected InputParam getCatalogInput(boolean isEn) {
        return getInputParam("catalog",
                isEn ? "catalog" : "目录类型",
                isEn ? "please enter catalog" : "请填入目录类型", 1,
                Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter catalog" : "请填入目录类型").build(),
                null);
    }

    @Override
    protected InputParam getDatabaseInput(boolean isEn) {
        return getInputParam("database",
                isEn ? "database" : "数据库",
                isEn ? "please enter database" : "请填入数据库", 1,
                null, null);
    }

    @Override
    protected InputParam getPasswordInput(boolean isEn) {
        return getInputParam("password",
                isEn ? "password" : "密码",
                isEn ? "please enter password" : "请填入密码", 1,
                null,
                null);
    }
}
