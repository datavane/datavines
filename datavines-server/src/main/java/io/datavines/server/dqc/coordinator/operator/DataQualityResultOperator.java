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

import io.datavines.common.entity.TaskRequest;
import io.datavines.engine.core.utils.JsonUtils;
import io.datavines.metric.api.ResultFormula;
import io.datavines.notification.api.entity.SlaConfigMessage;
import io.datavines.notification.api.entity.SlaNotificationMessage;
import io.datavines.notification.api.entity.SlaSenderMessage;
import io.datavines.notification.core.client.NotificationClient;
import io.datavines.server.api.dto.vo.TaskResultVO;
import io.datavines.server.repository.entity.Task;
import io.datavines.server.repository.entity.TaskResult;
import io.datavines.server.repository.service.SlaNotificationService;
import io.datavines.server.repository.service.TaskResultService;
import io.datavines.server.repository.service.TaskService;
import io.datavines.server.repository.service.impl.JobExternalService;
import io.datavines.server.enums.DqTaskState;
import io.datavines.server.enums.OperatorType;
import io.datavines.spi.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * DataQualityResultOperator
 */
@Component
public class DataQualityResultOperator {

    private final Logger logger = LoggerFactory.getLogger(DataQualityResultOperator.class);

    @Autowired
    private JobExternalService jobExternalService;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private TaskResultService taskResultService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SlaNotificationService slaNotificationService;


    /**
     * When the task type is data quality, it will get the statistics value、comparison value、
     * threshold、check type、operator and failure strategy，use the formula that
     * {result formula} {operator} {threshold} to get dqc result . If result is failure, it will alert
     * @param taskRequest taskRequest
     */
    public void operateDqExecuteResult(TaskRequest taskRequest) {

        TaskResult taskResult =
                jobExternalService.getTaskResultByTaskId(taskRequest.getTaskId());
        if (taskResult != null) {
            //check the result ,if result is failure do some operator by failure strategy
            checkDqExecuteResult(taskResult);
        }
    }

    /**
     * get the data quality check result
     * and if the result is failure that will alert or block
     * @param taskResult taskResult
     */
    private void checkDqExecuteResult(TaskResult taskResult) {
        if (isFailure(taskResult)) {
            taskResult.setState(DqTaskState.FAILURE.getCode());
            Long taskId = taskResult.getTaskId();
            sendErrorEmail(taskId);
        } else {
            taskResult.setState(DqTaskState.SUCCESS.getCode());
        }

        jobExternalService.updateTaskResult(taskResult);
    }


    private void sendErrorEmail(Long taskId){
        TaskResultVO resultVO = taskResultService.getResultVOByTaskId(taskId);
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
        Task task = taskService.getById(taskId);
        Long jobId = task.getJobId();

        Map<SlaSenderMessage, Set<SlaConfigMessage>> config = slaNotificationService.getSlasNotificationConfigurationByJobId(jobId);
        if (config.isEmpty()){
            return;
        }
        notificationClient.notify(message, config);
    }

    /**
     * It is used to judge whether the result of the data quality task is failed
     * @param taskResult
     * @return
     */
    private boolean isFailure(TaskResult taskResult) {

        Double actualValue = taskResult.getActualValue();
        Double expectedValue = null;
        if (taskResult.getExpectedValue() == null) {
            expectedValue = taskResult.getActualValue();
        } else {
            expectedValue = taskResult.getExpectedValue();
        }
        Double threshold = taskResult.getThreshold();

        OperatorType operatorType = OperatorType.of(taskResult.getOperator());

        ResultFormula resultFormula = PluginLoader.getPluginLoader(ResultFormula.class)
                            .getOrCreatePlugin(taskResult.getResultFormula());
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
