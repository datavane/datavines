package io.datavines.connector.plugin;

import io.datavines.common.param.form.type.InputParam;

public class HiveConfigBuilder extends JdbcConfigBuilder {

    @Override
    protected InputParam getPropertiesInput(boolean isEn) {
        return getInputParam("properties",
                isEn ? "properties" : "参数",
                isEn ? "please enter properties,like key=value&key1=value1" : "请填入参数，格式为key=value&key1=value1", 2, null,
                "hive.resultset.use.unique.column.names=false");
    }
}
