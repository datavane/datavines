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
package io.datavines.runner;

import io.datavines.common.config.Configurations;
import io.datavines.common.entity.JobExecutionRequest;
import io.datavines.common.utils.LoggerUtils;
import io.datavines.engine.api.engine.EngineExecutor;
import io.datavines.spi.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunner {

    private final Logger logger = LoggerFactory.getLogger(JobRunner.class);

    private final JobExecutionRequest jobExecutionRequest;

    private EngineExecutor engineExecutor;

    private final Configurations configurations;

    public JobRunner(JobExecutionRequest jobExecutionRequest, Configurations configurations){
        this.jobExecutionRequest = jobExecutionRequest;
        this.configurations = configurations;
    }

    public void run() {

        try {
            String taskLoggerName = LoggerUtils.buildJobExecutionLoggerName(
                    LoggerUtils.JOB_LOGGER_INFO_PREFIX,
                    jobExecutionRequest.getJobExecutionUniqueId());

            // custom logger
            Logger taskLogger = LoggerFactory.getLogger(taskLoggerName);
            Thread.currentThread().setName(taskLoggerName);

            engineExecutor = PluginLoader
                    .getPluginLoader(EngineExecutor.class)
                    .getNewPlugin(jobExecutionRequest.getEngineType());

            engineExecutor.init(jobExecutionRequest, taskLogger, configurations);
            engineExecutor.execute();
            engineExecutor.after();

            Long jobExecutionId = jobExecutionRequest.getJobExecutionId();
            // 获取存储插件，构造读取参数，读取结果数据，结合公式判断任务是否失败，如果失败则进行告警
            // 获取告警插件，构造告警信息进行发送
        } catch (Exception e) {
            logger.error("task execute failure", e);

        } finally {

        }
    }
}
