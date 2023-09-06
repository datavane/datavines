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
package io.datavines.engine.executor.core.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import io.datavines.common.CommonConstants;
import io.datavines.common.config.Configurations;
import io.datavines.common.entity.JobExecutionRequest;
import io.datavines.common.entity.ProcessResult;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.Stopper;
import io.datavines.common.utils.StringUtils;
import io.datavines.common.utils.YarnUtils;
import io.datavines.engine.executor.core.enums.LivyStates;
import io.datavines.engine.executor.core.helper.LivyTaskSubmitHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LivyCommandProcess extends BaseCommandProcess {

    private LivyTaskSubmitHelper livyTaskSubmitHelper;

    private int appIdRetryCount;

    public LivyCommandProcess(Consumer<List<String>> logHandler,
                              Logger logger,
                              JobExecutionRequest jobExecutionRequest,
                              Configurations configurations){
        super(logHandler, logger, jobExecutionRequest, configurations);
        this.appIdRetryCount = configurations.getInt("livy.task.appId.retry.count", 2);
        this.livyTaskSubmitHelper = new LivyTaskSubmitHelper(configurations);
    }

    @Override
    public void cancel() {

        long jobExecutionId = jobExecutionRequest.getJobExecutionId();
        String applicationId = jobExecutionRequest.getApplicationId();

        if (StringUtils.isEmpty(applicationId)) {
            applicationId = YarnUtils.getYarnAppId(jobExecutionRequest.getTenantCode(),
                    jobExecutionRequest.getJobExecutionUniqueId());
        }

        cancel(jobExecutionId, applicationId);
    }

    private void cancel(Long sessionId, String appId) {
        livyTaskSubmitHelper.deleteByLivy(sessionId, appId);
    }

    public Map<String, Object> post2LivyWithRetry(String livyArgs) {

        String result = post2Livy(livyArgs);
        Map<String, Object> resultMap = null;

        if (result != null) {
            resultMap = livyTaskSubmitHelper.retryLivyGetAppId(result, appIdRetryCount);
        }

        return resultMap;
    }

    TypeReference<HashMap<String, Object>> type =
            new TypeReference<HashMap<String, Object>>() {
            };

    public boolean processResultOfLivyState(Object sessionId, ProcessResult processResult) {
        boolean result = true;
        try {
            while(Stopper.isRunning()){
                Map<String, Object> resultMap = livyTaskSubmitHelper.getResultByLivyId(sessionId, type);
                if (resultMap != null) {
                    LivyStates.State state = LivyStates.toLivyState(resultMap);
                    if (LivyStates.State.SUCCESS.equals(state)) {
                        Object appId = resultMap.get("appId");
                        processResult.setApplicationId(String.valueOf(appId));
                        processResult.setExitStatusCode(ExecutionStatus.SUCCESS.getCode());
                        break;
                    }

                    if (LivyStates.State.DEAD.equals(state) || LivyStates.State.ERROR.equals(state)
                    || LivyStates.State.KILLED.equals(state) || LivyStates.State.UNKNOWN.equals(state)) {
                        Object appId = resultMap.get("appId");
                        processResult.setApplicationId(String.valueOf(appId));
                        processResult.setExitStatusCode(ExecutionStatus.FAILURE.getCode());
                        return false;
                    }
                }

                Thread.sleep(CommonConstants.SLEEP_TIME_MILLIS);
            }
        } catch (Exception e) {
            logger.error(String.format("livy session id : %s  status failed ", sessionId),e);
            processResult.setExitStatusCode(ExecutionStatus.FAILURE.getCode());
            result = false;
        }
        return result;
    }

    private String post2Livy(String livyArgs) {
        return livyTaskSubmitHelper.postToLivy(livyArgs);
    }

    @Override
    protected String commandInterpreter() {
        return null;
    }

    @Override
    protected String buildCommandFilePath() {
        return null;
    }

    @Override
    protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {

    }


}
