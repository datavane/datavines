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
package io.datavines.notification.plugin.email;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datavines.common.CommonConstants;
import io.datavines.common.param.form.ParamsOptions;
import io.datavines.common.param.form.PluginParams;
import io.datavines.common.param.form.PropsType;
import io.datavines.common.param.form.Validate;
import io.datavines.common.param.form.props.InputParamsProps;
import io.datavines.common.param.form.type.InputParam;
import io.datavines.common.param.form.type.RadioParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.notification.api.entity.*;
import io.datavines.notification.api.spi.SlasHandlerPlugin;
import io.datavines.notification.plugin.email.entity.NotificationConfig;
import io.datavines.notification.plugin.email.entity.ReceiverConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class EmailSlasHandlerPlugin implements SlasHandlerPlugin {
    private  final String STRING_YES = "YES";
    private  final String STRING_NO = "NO";
    private  final String STRING_TRUE = "TRUE";
    private  final String STRING_FALSE = "FALSE";


    @Override
    public String getConfigReceiverJson() {
        List<PluginParams> paramsList = new ArrayList<>();

        InputParam receiver = InputParam.newBuilder("receiver", "receiver")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;
        paramsList.add(receiver);
        try {
            result = mapper.writeValueAsString(paramsList);
        } catch (JsonProcessingException e) {
                log.error("json parse error : {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public SlasNotificationResult notify(SlasNotificationMessage slasNotificationMessage, Map<SlasSenderMessage, Set<SlasReceiverMessage>> config) {
        Set<SlasSenderMessage> emailSenderSet = config.keySet().stream().filter(x -> "email".equals(x.getType())).collect(Collectors.toSet());
        SlasNotificationResult result = new SlasNotificationResult();
        ArrayList<SlasNotificationResultRecord> records = new ArrayList<>();
        result.setStatus(true);
        String subject = slasNotificationMessage.getSubject();
        String message = slasNotificationMessage.getMessage();
        for(SlasSenderMessage senderMessage: emailSenderSet){
            EMailSender eMailSender = new EMailSender(senderMessage);
            Set<SlasReceiverMessage> slasReceiverMessageSet = config.get(senderMessage);
            HashSet<String> toReceivers = new HashSet<>();
            HashSet<String> ccReceivers = new HashSet<>();
            for(SlasReceiverMessage receiver: slasReceiverMessageSet){
                String receiverConfigStr = receiver.getConfig();
                String notificationConfigStr = receiver.getNotificationConfig();
                ReceiverConfig receiverConfig = JSONUtils.parseObject(receiverConfigStr, ReceiverConfig.class);
                NotificationConfig notificationConfig = JSONUtils.parseObject(notificationConfigStr, NotificationConfig.class);
                String realReceiver = receiverConfig.getReceiver();
                String configReceiver = realReceiver;
                if ("recipient".equals(notificationConfig)){
                    toReceivers.add(realReceiver);
                }
                if("cc".equals(notificationConfig)){
                    ccReceivers.add(realReceiver);
                }
            }
            SlasNotificationResultRecord record = eMailSender.sendMails(toReceivers, ccReceivers, subject, message);
            if (record.getStatus().equals(false)){
                String to = "";
                String recordMessage = "";

                if (!CollectionUtils.isEmpty(toReceivers)){
                    to = toReceivers.stream().collect(Collectors.joining(","));
                    recordMessage = String.format("send to %s fail", to);
                }
                String cc = "";
                if (!CollectionUtils.isEmpty(ccReceivers)){
                    cc = ccReceivers.stream().collect(Collectors.joining(","));
                    recordMessage += String.format("copy to %s fail", cc);
                }
                record.setMessage(recordMessage);
                result.setStatus(false);
            }
            records.add(record);
        }
        result.setRecords(records);
        return result;
    }

    @Override
    public String getConfigSenderJson() {


        List<PluginParams> paramsList = new ArrayList<>();

        InputParam mailSmtpHost = InputParam.newBuilder("serverHost", "mail.smtp.host")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam mailSmtpPort = InputParam.newBuilder("serverPort", "mail.smtp.port")
                .setValue("25")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam mailSender = InputParam.newBuilder("sender", "mail.sender")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam enableSmtpAuth = RadioParam.newBuilder("enableSmtpAuth", "mail.smtp.auth")
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_TRUE)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam mailUser = InputParam.newBuilder("user", "mail.user")
                .setPlaceholder("if enable use authentication, you need input user")
                .build();

        InputParam mailPassword = InputParam.newBuilder("passwd", "mail.passwd")
                .setPlaceholder("if enable use authentication, you need input password")
                .build();

        RadioParam enableTls = RadioParam.newBuilder("starttlsEnable", "mail.smtp.starttls.enable")
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam enableSsl = RadioParam.newBuilder("sslEnable", "mail.smtp.ssl.enable")
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam sslTrust = InputParam.newBuilder("smtpSslTrust", "mail.smtp.ssl.trust")
                .setValue("*")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        paramsList.add(mailSmtpHost);
        paramsList.add(mailSmtpPort);
        paramsList.add(mailSender);
        paramsList.add(enableSmtpAuth);
        paramsList.add(mailUser);
        paramsList.add(mailPassword);
        paramsList.add(enableTls);
        paramsList.add(enableSsl);
        paramsList.add(sslTrust);

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

        RadioParam receiverType = RadioParam.newBuilder("receiveType", "receiveType")
                .addParamsOptions(new ParamsOptions("recipient", "recipient", false))
                .addParamsOptions(new ParamsOptions("cc", "cc", false))
                .setValue("recipient")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;
        try {
            result = mapper.writeValueAsString(receiverType);
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
