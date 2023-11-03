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
package io.datavines.notification.plugin.wecombot;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datavines.common.param.form.PluginParams;
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.type.InputParam;
import io.datavines.notification.api.entity.*;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class WecomBotSlasHandlerPlugin implements SlasHandlerPlugin {
    @Override
    public SlaNotificationResult notify(SlaNotificationMessage slaNotificationMessage, Map<SlaSenderMessage, Set<SlaConfigMessage>> config) {
        Set<SlaSenderMessage> wecomBotSenderSet = config.keySet().stream().filter(x -> "wecombot".equals(x.getType())).collect(Collectors.toSet());
        SlaNotificationResult result = new SlaNotificationResult();
        ArrayList<SlaNotificationResultRecord> records = new ArrayList<>();
        result.setStatus(true);
        String subject = slaNotificationMessage.getSubject();
        String message = slaNotificationMessage.getMessage();
        for (SlaSenderMessage senderMessage: wecomBotSenderSet) {
            WecomBotSender wecomBotSender = new WecomBotSender(senderMessage);
            // get webhook list
            String[] webhookArr = wecomBotSender.getWebhookList().split(";");
            HashSet<String> toReceivers = new HashSet<>(Arrays.asList(webhookArr));
            SlaNotificationResultRecord record = wecomBotSender.sendMsg(toReceivers, subject, message);
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
    public String getConfigSenderJson() {
        // notify config item
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam webhook = InputParam.newBuilder("webhook", "webhook")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        paramsList.add(webhook);
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
    public String getConfigJson() {
        // sla create notify config item
        List<PluginParams> paramsList = new ArrayList<>();
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
