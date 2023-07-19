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
package io.datavines.notification.plugin.lark;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datavines.common.param.form.ParamsOptions;
import io.datavines.common.param.form.PluginParams;
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.type.InputParam;
import io.datavines.common.param.form.type.RadioParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.notification.api.entity.*;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.notification.plugin.lark.entity.ReceiverConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LarkSlasHandlerPlugin implements SlasHandlerPlugin {

    private final String STRING_YES = "YES";

    private final String STRING_NO = "NO";

    private final String STRING_TRUE = "TRUE";

    private final String STRING_FALSE = "FALSE";

    @Override
    public SlaNotificationResult notify(SlaNotificationMessage slaNotificationMessage, Map<SlaSenderMessage, Set<SlaConfigMessage>> config) { // config里面不仅是邮箱途径，还会是目前扩展的飞书途径，SlaSenderMessage就表示各个途径，然后Set<SlaConfigMessage>表示发送给A这批人，发送给B这批人的集合
        Set<SlaSenderMessage> larkSenderSet = config.keySet().stream().filter(x -> "lark".equals(x.getType())).collect(Collectors.toSet());
        SlaNotificationResult result = new SlaNotificationResult();
        ArrayList<SlaNotificationResultRecord> records = new ArrayList<>();
        result.setStatus(true);
        String subject = slaNotificationMessage.getSubject();
        String message = slaNotificationMessage.getMessage();
        for (SlaSenderMessage senderMessage: larkSenderSet) { // 开始循环每个告警途径
            LarkSender larkSender = new LarkSender(senderMessage);
            Set<SlaConfigMessage> slaConfigMessageSet = config.get(senderMessage);
            HashSet<ReceiverConfig> toReceivers = new HashSet<>();
            for (SlaConfigMessage receiver: slaConfigMessageSet) { // 开始循环要发送给哪组人，把通知人放到同一个list里，后续统一告警出去。
                String receiverConfigStr = receiver.getConfig();
                ReceiverConfig receiverConfig = JSONUtils.parseObject(receiverConfigStr, ReceiverConfig.class);
                toReceivers.add(receiverConfig);
            }

            SlaNotificationResultRecord record = larkSender.sendCardMsg(toReceivers, subject, message);
            if (record.getStatus().equals(false)) {
                record.setMessage(record.getMessage());
                result.setStatus(false);
            }
            records.add(record);
        }
        result.setRecords(records);
        return result;
    }

    @Override
    public String getConfigSenderJson() { // 告警途径，不变的配置，比如邮箱途径的服务器配置是不变的配置，发送给谁这个是会变化的。

        List<PluginParams> paramsList = new ArrayList<>();

        InputParam appId = InputParam.newBuilder("appId", "app_id")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        InputParam appSecret = InputParam.newBuilder("appSecret", "app_secret")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        paramsList.add(appId);
        paramsList.add(appSecret);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(paramsList);
        } catch (JsonProcessingException e) {
            log.error("json parse error : {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public String getConfigJson() { // 告警人，告警给谁的配置项。

        List<PluginParams> paramsList = new ArrayList<>();

        InputParam groupName = InputParam.newBuilder("groupName", "groupName")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        InputParam token = InputParam.newBuilder("token", "token")
                .setPlaceholder("Please provide the group webhook without the URL prefix \"https...hook/\"")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        RadioParam atAll = RadioParam.newBuilder("atAll", "atAll")
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        paramsList.add(groupName);
        paramsList.add(token);
        paramsList.add(atAll);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(paramsList);
        } catch (JsonProcessingException e) {
            log.error("json parse error : {}", e.getMessage(), e);
        }

        return result;
    }
}
