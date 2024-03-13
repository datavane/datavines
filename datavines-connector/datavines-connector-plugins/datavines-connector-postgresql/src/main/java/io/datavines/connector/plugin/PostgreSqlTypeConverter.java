package io.datavines.connector.plugin;

import io.datavines.common.enums.DataType;
import io.datavines.common.utils.StringUtils;

import javax.xml.crypto.Data;

/**
 * @author xxzuo
 * @description
 * @date 2024/3/13 17:28
 **/
public class PostgreSqlTypeConverter extends JdbcTypeConverter{
    @Override
    public DataType convert(String originType) {
        if (StringUtils.isEmpty(originType)) {
            throw new UnsupportedOperationException("sql type id null error");
        }
        switch (originType.toUpperCase()) {
            case "INT8":
            case "INT4":
            case "INT2":
                return DataType.INT_TYPE;
            case "FLOAT8":
            case "FLOAT4":
                return DataType.FLOAT_TYPE;
            case "NUMBER":
                return DataType.DOUBLE_TYPE;
            case "TIMESTAMPTZ":
                return DataType.TIMESTAMP_TYPE;
            case "BPCHAR":
                return DataType.STRING_TYPE;
            case "NUMERIC":
                return DataType.BIG_DECIMAL_TYPE;
            default:
                return super.convert(originType);
        }
    }

    @Override
    public String convertToOriginType(DataType dataType) {
        return  super.convertToOriginType(dataType);
    }
}
