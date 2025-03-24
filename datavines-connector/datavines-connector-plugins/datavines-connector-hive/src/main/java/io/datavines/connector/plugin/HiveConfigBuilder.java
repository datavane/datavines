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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datavines.common.param.form.PluginParams;
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.type.InputParam;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HiveConfigBuilder extends JdbcConfigBuilder {

    @Override
    public String build(boolean isEn) {
        List<PluginParams> params = new ArrayList<>();
        params.add(getHostInput(isEn));
        params.add(getPortInput(isEn));
        if (getCatalogInput(isEn) != null) {
            params.add(getCatalogInput(isEn));
        }

        params.add(getDatabaseInput(isEn));

        if (getSchemaInput(isEn) != null) {
            params.add(getSchemaInput(isEn));
        }

        params.add(getUserInput(isEn));
        params.add(getPasswordInput(isEn));

        params.add(getKeytabFile(isEn));
        params.add(getKeytabPrincipal(isEn));
        params.add(getKrb5Conf(isEn));

        params.add(getPropertiesInput(isEn));

        params.addAll(getOtherParams(isEn));

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("json parse error : ", e);
        }
        return result;
    }

    @Override
    protected InputParam getPropertiesInput(boolean isEn) {
        return getInputParam("properties",
                isEn ? "properties" : "参数",
                isEn ? "please enter properties,like key=value;key1=value1" : "请填入参数，格式为key=value;key1=value1", 2, null,
                "hive.resultset.use.unique.column.names=false");
    }

    @Override
    protected List<PluginParams> getOtherParams(boolean isEn) {

        List<PluginParams> list = new ArrayList<>();

        InputParam enableSparkHiveSupport = getInputParam("enable_spark_hive_support",
                "spark.enable.hive.support",
                isEn ? "please enter true or false" : "请填入 true 或者 false", 2, null,
                "true");

        list.add(enableSparkHiveSupport);
        return list;
    }

    private InputParam getKeytabFile(boolean isEn) {
        return getInputParam("keytab_file",
                isEn ? "keytab File Path" : "keytab 文件地址",
                isEn ? "please enter keytab File Path" : "请填入 keytab 文件地址", 1,
                null,
                null);
    }

    private InputParam getKeytabPrincipal(boolean isEn) {
        return getInputParam("keytab_principal",
                "keytab Principal",
                isEn ? "please enter keytab Principal" : "请填入 keytab 文件对应的 Principal", 1,
                null,
                null);
    }

    private InputParam getKrb5Conf(boolean isEn) {
        return getInputParam("krb5_conf",
                isEn ? "krb5.conf File Path" : "krb5.conf 文件地址",
                isEn ? "please enter krb5.conf File Path" : "请填入 krb5.conf 文件地址", 1,
                null,
                null);
    }

}
