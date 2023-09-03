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
package io.datavines.server.dqc.coordinator.runner;

import io.datavines.common.utils.*;
import io.datavines.server.dqc.coordinator.cache.JobExecuteManager;
import io.datavines.server.registry.Register;
import io.datavines.server.enums.CommandType;
import io.datavines.server.repository.entity.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.server.repository.service.impl.JobExternalService;
import io.datavines.server.utils.SpringApplicationContext;
import io.datavines.server.repository.entity.Command;

import java.util.Map;

import static io.datavines.common.CommonConstants.*;
import static io.datavines.common.utils.CommonPropertyUtils.*;

public class JobScheduler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private static final int[] RETRY_BACKOFF = {1, 2, 3, 5, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10};

    private final JobExternalService jobExternalService;

    private final JobExecuteManager jobExecuteManager;

    private final Register register;

    public JobScheduler(JobExecuteManager jobExecuteManager, Register register){
        this.jobExternalService = SpringApplicationContext.getBean(JobExternalService.class);
        this.jobExecuteManager = jobExecuteManager;
        this.register = register;
    }

    @Override
    public void run() {
        logger.info("job scheduler started");

        int retryNum = 0;
        while (Stopper.isRunning()) {
            Command command = null;
            try {
                boolean runCheckFlag = OSUtils.checkResource(
                        CommonPropertyUtils.getDouble(MAX_CPU_LOAD_AVG, MAX_CPU_LOAD_AVG_DEFAULT),
                        CommonPropertyUtils.getDouble(RESERVED_MEMORY, RESERVED_MEMORY_DEFAULT));

                if (!runCheckFlag) {
                    ThreadUtils.sleep(SLEEP_TIME_MILLIS*10);
                    continue;
                }

                command = jobExternalService.getCommand(register.getTotalSlot(), register.getSlot());
                if (command != null) {
                    String parameter = command.getParameter();
                    String engineType = LOCAL;
                    if (StringUtils.isNotEmpty(parameter)) {
                        Map<String,String> parameterMap = JSONUtils.toMap(parameter);
                        if (StringUtils.isNotEmpty(parameterMap.get(ENGINE))) {
                            engineType = parameterMap.get(ENGINE);
                        }
                    }

                    if (CommandType.START == command.getType()) {
                        JobExecution jobExecution = jobExternalService.executeCommand(command);
                        if (jobExecution == null) {
                            logger.warn(String.format("jobExecution not found , command : %s", JSONUtils.toJsonString(command)));
                            jobExternalService.deleteCommandById(command.getId());
                            continue;
                        }

                        if (!executionOutOfThreshold(engineType)) {
                            logger.info("start submit jobExecution : {} ", JSONUtils.toJsonString(jobExecution));
                            jobExecuteManager.addExecuteCommand(jobExecution);
                            logger.info(String.format("submit success, jobExecution : %s", jobExecution.getName()) );
                            jobExternalService.deleteCommandById(command.getId());
                        }
                    } else if (CommandType.STOP == command.getType()) {
                        jobExecuteManager.addKillCommand(command.getJobExecutionId());
                        logger.info(String.format("kill task : %s", command.getJobExecutionId()) );
                        jobExternalService.deleteCommandById(command.getId());
                    }

                    ThreadUtils.sleep(SLEEP_TIME_MILLIS);
                } else {
                    ThreadUtils.sleep(SLEEP_TIME_MILLIS * 4);
                }

                retryNum = 0;
            } catch (Exception e){
                retryNum++;
                if (command != null) {
                    command.setType(CommandType.ERROR);
                    jobExternalService.updateCommand(command);
                }

                logger.error("schedule job error ", e);
                ThreadUtils.sleep(SLEEP_TIME_MILLIS * RETRY_BACKOFF[retryNum % RETRY_BACKOFF.length]);
            } finally {
                jobExternalService.refreshCommonProperties();
            }
        }
    }

    public boolean executionOutOfThreshold(String engineType) {
        if (StringUtils.isEmpty(engineType)) {
            return false;
        }

        String engineExecutionThresholdKey = String.format("%s.execution.threshold", engineType);
        int engineExecutionThreshold = CommonPropertyUtils.getInt(engineExecutionThresholdKey, Integer.MAX_VALUE);
        int engineExecutionCount = jobExecuteManager.getExecutionCountByEngine(engineType) * register.getTotalSlot();

        boolean result = engineExecutionCount >= engineExecutionThreshold;

        if (result) {
            logger.info("engine is {}, engine.execution.threshold is : {} , engine.execution.count is {}", engineType, engineExecutionThreshold, engineExecutionCount);
        }

        return result;
    }
}
