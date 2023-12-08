package io.datavines.connector.plugin;

import io.datavines.common.enums.DataType;
import io.datavines.common.utils.StringUtils;

public class OracleTypeConverter extends JdbcTypeConverter {
    @Override
    public DataType convert(String originType) {
        if (StringUtils.isEmpty(originType)) {
            throw new UnsupportedOperationException("sql type id null error");
        }
        switch (originType.toUpperCase()) {
            case "INT4":
            case "INT2":
                return DataType.INT_TYPE;
            case "BINARY_FLOAT":
                return DataType.FLOAT_TYPE;
            case "NUMBER":
                return DataType.DOUBLE_TYPE;
            case "TIMETZ":
                return DataType.TIME_TYPE;
            case "TIMESTAMPTZ":
                return DataType.TIMESTAMP_TYPE;
            case "NCHAR":
            case "VARCHAR2":
            case "NVARCHAR2":
                return DataType.STRING_TYPE;
            default:
                return super.convert(originType);
        }
    }

    @Override
    public String convertToOriginType(DataType dataType) {
        return  super.convertToOriginType(dataType);
    }
}
