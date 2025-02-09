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
package io.datavines.engine.flink.executor;

import io.datavines.common.config.DataVinesJobConfig;
import io.datavines.common.utils.*;
import io.datavines.engine.executor.core.executor.ShellCommandProcess;
import io.datavines.engine.flink.executor.parameter.FlinkArgsUtils;
import io.datavines.engine.flink.executor.parameter.FlinkParameters;
import org.slf4j.Logger;
import io.datavines.common.config.Configurations;
import io.datavines.common.entity.JobExecutionRequest;
import io.datavines.common.entity.ProcessResult;
import io.datavines.engine.executor.core.base.AbstractYarnEngineExecutor;
import io.datavines.engine.executor.core.executor.BaseCommandProcess;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class FlinkEngineExecutor extends AbstractYarnEngineExecutor {

    private static final String FLINK_COMMAND = "${FLINK_HOME}/bin/flink";

    private Configurations configurations;

    private JobExecutionRequest jobExecutionRequest;

    private Logger logger;

    private ProcessResult processResult;

    private BaseCommandProcess shellCommandProcess;

    private boolean cancel;

    @Override
    public void init(JobExecutionRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception {
        String threadLoggerInfoName = String.format(LoggerUtils.JOB_LOG_INFO_FORMAT, jobExecutionRequest.getJobExecutionUniqueId());
        Thread.currentThread().setName(threadLoggerInfoName);

        this.jobExecutionRequest = jobExecutionRequest;
        this.logger = logger;
        this.configurations = configurations;
        this.processResult = new ProcessResult();
        this.shellCommandProcess = new ShellCommandProcess(
            this::logHandle,
            logger,
            jobExecutionRequest,
            configurations
        );
    }

    @Override
    public void execute() throws Exception {
        try {
            String command = buildCommand();
            logger.info("flink task command: {}", command);
            this.processResult = shellCommandProcess.run(command);
            logger.info("process result: {}", JSONUtils.toJsonString(this.processResult));
        } catch (Exception e) {
            logger.error("process fail", e);
            throw e;
        }
    }

    @Override
    protected String buildCommand() {
        FlinkParameters flinkParameters = JSONUtils.parseObject(jobExecutionRequest.getEngineParameter(), FlinkParameters.class);
        assert flinkParameters != null;

        String basePath = System.getProperty("user.dir").replace(File.separator + "bin", File.separator + "libs");
        flinkParameters.setMainJar(basePath + File.separator + configurations.getString("data.quality.flink.jar.name"));

//        String pluginDir = basePath.endsWith("libs") ?
//                basePath.replace("libs","plugins") + File.separator + "flink":
//                basePath + File.separator + "plugins" + File.separator + "flink";
//
//        logger.info("flink engine plugin dir : {}", pluginDir);
//
//        if (FileUtils.isExist(pluginDir)) {
//            List<String> filePathList = FileUtils.getFileList(pluginDir);
//            if (CollectionUtils.isNotEmpty(filePathList)) {
//                String jars = " --classpath " + String.join(",", filePathList);
//                flinkParameters.setJars(jars);
//            }
//        }

        DataVinesJobConfig configuration =
                JSONUtils.parseObject(jobExecutionRequest.getApplicationParameter(), DataVinesJobConfig.class);

        flinkParameters.setMainArgs("\""
                + Base64.getEncoder().encodeToString(Objects.requireNonNull(JSONUtils.toJsonString(configuration)).getBytes()) + "\"");

        flinkParameters.setMainClass("io.datavines.engine.flink.core.FlinkDataVinesBootstrap");
        flinkParameters.setTags(jobExecutionRequest.getJobExecutionUniqueId());

        List<String> args = new ArrayList<>();

        args.add(FLINK_COMMAND);

        args.addAll(FlinkArgsUtils.buildArgs(flinkParameters));

        String command = String.join(" ", args);

        logger.info("flink data quality task command: {}", command);

        return command;
    }

    @Override
    public void after() {
    }

    @Override
    public ProcessResult getProcessResult() {
        return this.processResult;
    }

    @Override
    public JobExecutionRequest getTaskRequest() {
        return this.jobExecutionRequest;
    }

    @Override
    public void cancel() throws Exception {
        cancel = true;
        if (shellCommandProcess != null) {
            shellCommandProcess.cancel();
        }
        killYarnApplication();
    }

    private void killYarnApplication() {
        try {
            String applicationId = YarnUtils.getYarnAppId(jobExecutionRequest.getTenantCode(), 
                                                        jobExecutionRequest.getJobExecutionUniqueId());

            if (StringUtils.isNotEmpty(applicationId)) {
                String cmd = String.format("sudo -u %s yarn application -kill %s", 
                                         jobExecutionRequest.getTenantCode(), 
                                         applicationId);

                logger.info("Killing yarn application: {}", applicationId);
                Runtime.getRuntime().exec(cmd);
            }
        } catch (Exception e) {
            logger.error("Failed to kill yarn application", e);
        }
    }

    @Override
    public void logHandle(List<String> logs) {
        if (logs != null && !logs.isEmpty()) {
            for (String log : logs) {
                logger.info(log);
            }
        }
    }

    @Override
    public boolean isCancel() {
        return this.cancel;
    }
}
