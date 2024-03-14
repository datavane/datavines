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
package io.datavines.connector.plugin;

import io.datavines.common.enums.DataType;
import io.datavines.common.utils.StringUtils;

public class PostgreSqlTypeConverter extends JdbcTypeConverter{
    @Override
    public DataType convert(String originType) {
        if (StringUtils.isEmpty(originType)) {
            throw new UnsupportedOperationException("sql type id null error");
        }
        switch (originType.toUpperCase()) {
            case "INT4":
            case "INT2":
            case "OID":
            case "SERIAL":
                return DataType.INT_TYPE;
            case "BIGSERIAL":
            case "INT8":
                return DataType.LONG_TYPE;
            case "BOOL":
                return DataType.BOOLEAN_TYPE;
            case "FLOAT8":
            case "FLOAT4":
            case "REAL":
                return DataType.FLOAT_TYPE;
            case "NUMBER":
            case "MONEY":
                return DataType.DOUBLE_TYPE;
            case "TIMESTAMPTZ":
                return DataType.TIMESTAMP_TYPE;
            case "TIMETZ":
                return DataType.TIME_TYPE;
            case "BPCHAR":
            case "UUID":
            case "JSONB":
            case "XML":
                return DataType.STRING_TYPE;
            case "NUMERIC":
                return DataType.BIG_DECIMAL_TYPE;
            case "CIDR":
            case "INET":
            case "JSONPATH":
            case "CIRCLE":
            case "POINT":
            case "LINE":
            case "BOX":
            case "PATH":
            case "POLYGON":
            case "LSEG":
            case "VARBIT":
                return DataType.OBJECT;
            default:
                return super.convert(originType);
        }
    }

    @Override
    public String convertToOriginType(DataType dataType) {
        return  super.convertToOriginType(dataType);
    }
}
