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
package io.datavines.server.dqc.coordinator.operator;

import io.datavines.common.entity.JobExecutionRequest;
import io.datavines.engine.core.utils.JsonUtils;
import io.datavines.metric.api.ResultFormula;
import io.datavines.notification.api.entity.SlaConfigMessage;
import io.datavines.notification.api.entity.SlaNotificationMessage;
import io.datavines.notification.api.entity.SlaSenderMessage;
import io.datavines.notification.core.client.NotificationClient;
import io.datavines.server.api.dto.vo.JobExecutionResultVO;
import io.datavines.server.enums.DqJobExecutionState;
import io.datavines.server.repository.entity.JobExecution;
import io.datavines.server.repository.entity.JobExecutionResult;
import io.datavines.server.repository.service.SlaNotificationService;
import io.datavines.server.repository.service.JobExecutionResultService;
import io.datavines.server.repository.service.JobExecutionService;
import io.datavines.server.repository.service.impl.JobExternalService;
import io.datavines.server.enums.OperatorType;
import io.datavines.spi.PluginLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Component
public class DataQualityResultOperator {

    @Autowired
    private JobExternalService jobExternalService;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private JobExecutionResultService jobExecutionResultService;

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private SlaNotificationService slaNotificationService;
    
    /**
     * When the task type is data quality, it will get the statistics value、comparison value、
     * threshold、check type、operator and failure strategy，use the formula that
     * {result formula} {operator} {threshold} to get dqc result . If result is failure, it will alert
     * @param jobExecutionRequest jobExecutionRequest
     */
    public void operateDqExecuteResult(JobExecutionRequest jobExecutionRequest) {

        JobExecutionResult jobExecutionResult =
                jobExternalService.getJobExecutionResultByJobExecutionId(jobExecutionRequest.getJobExecutionId());
        if (jobExecutionResult != null) {
            //check the result ,if result is failure do some operator by failure strategy
            checkDqExecuteResult(jobExecutionResult);
        }
    }

    /**
     * get the data quality check result
     * and if the result is failure that will alert or block
     * @param jobExecutionResult jobExecutionResult
     */
    private void checkDqExecuteResult(JobExecutionResult jobExecutionResult) {
        if (isFailure(jobExecutionResult)) {
            jobExecutionResult.setState(DqJobExecutionState.FAILURE.getCode());
            Long taskId = jobExecutionResult.getJobExecutionId();
            sendErrorEmail(taskId);
        } else {
            jobExecutionResult.setState(DqJobExecutionState.SUCCESS.getCode());
        }

        jobExternalService.updateJobExecutionResult(jobExecutionResult);
    }
    
    private void sendErrorEmail(Long taskId){
        JobExecutionResultVO resultVO = jobExecutionResultService.getResultVOByJobExecutionId(taskId);
        LinkedList<String> messageList = new LinkedList<>();
        messageList.add(resultVO.getMetricName());
        messageList.add(resultVO.getCheckSubject());
        messageList.add(resultVO.getCheckResult());
        messageList.add(resultVO.getExpectedType());
        messageList.add(resultVO.getResultFormulaFormat());
        String jsonMessage = JsonUtils.toJsonString(messageList);
        SlaNotificationMessage message = new SlaNotificationMessage();
        message.setMessage(jsonMessage);
        message.setSubject(String.format("datavines metric %s failure", resultVO.getMetricName()));
        JobExecution jobExecution = jobExecutionService.getById(taskId);
        Long jobId = jobExecution.getJobId();

        Map<SlaSenderMessage, Set<SlaConfigMessage>> config = slaNotificationService.getSlasNotificationConfigurationByJobId(jobId);
        if (config.isEmpty()){
            return;
        }
        notificationClient.notify(message, config);
    }

    /**
     * It is used to judge whether the result of the data quality task is failed
     * @param jobExecutionResult
     * @return
     */
    private boolean isFailure(JobExecutionResult jobExecutionResult) {

        Double actualValue = jobExecutionResult.getActualValue();
        Double expectedValue = null;
        if (jobExecutionResult.getExpectedValue() == null) {
            expectedValue = jobExecutionResult.getActualValue();
        } else {
            expectedValue = jobExecutionResult.getExpectedValue();
        }
        Double threshold = jobExecutionResult.getThreshold();

        OperatorType operatorType = OperatorType.of(jobExecutionResult.getOperator());

        ResultFormula resultFormula = PluginLoader.getPluginLoader(ResultFormula.class)
                            .getOrCreatePlugin(jobExecutionResult.getResultFormula());
        return getCompareResult(operatorType, resultFormula.getResult(actualValue, expectedValue), threshold);
    }

    private boolean getCompareResult(OperatorType operatorType, Double srcValue, Double targetValue) {
        BigDecimal src = BigDecimal.valueOf(srcValue);
        BigDecimal target = BigDecimal.valueOf(targetValue);
        switch (operatorType) {
            case EQ:
                return src.compareTo(target) == 0;
            case LT:
                return src.compareTo(target) <= -1;
            case LTE:
                return src.compareTo(target) == 0 || src.compareTo(target) <= -1;
            case GT:
                return src.compareTo(target) >= 1;
            case GTE:
                return src.compareTo(target) == 0 || src.compareTo(target) >= 1;
            case NE:
                return src.compareTo(target) != 0;
            default:
                return true;
        }
    }
}
