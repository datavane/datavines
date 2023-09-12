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
package io.datavines.engine.livy.executor;

import java.util.*;
import java.util.stream.Collectors;

import io.datavines.common.config.EnvConfig;
import io.datavines.common.entity.JobExecutionRequest;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.engine.executor.core.base.AbstractLivyEngineExecutor;
import io.datavines.engine.executor.core.enums.LivyStates;
import io.datavines.engine.executor.core.executor.LivyCommandProcess;
import io.datavines.engine.livy.executor.parameter.LivySparkParameters;
import io.datavines.engine.livy.executor.parameter.ProgramType;
import io.datavines.engine.livy.executor.utils.StringUtils;
import org.slf4j.Logger;

import io.datavines.common.config.Configurations;
import io.datavines.common.config.DataVinesJobConfig;
import io.datavines.common.entity.ProcessResult;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.LoggerUtils;
import io.datavines.engine.livy.executor.parameter.SparkParameters;

import static io.datavines.engine.livy.executor.parameter.SparkConstants.*;

public class LivyEngineExecutor extends AbstractLivyEngineExecutor {

    private Configurations configurations;

    @Override
    public void init(JobExecutionRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception {

        String threadLoggerInfoName = String.format(LoggerUtils.JOB_LOG_INFO_FORMAT, jobExecutionRequest.getJobExecutionUniqueId());
        Thread.currentThread().setName(threadLoggerInfoName);

        this.jobExecutionRequest = jobExecutionRequest;
        this.logger = logger;
        this.livyCommandProcess = new LivyCommandProcess(this::logHandle,
                logger, jobExecutionRequest, configurations);
        this.configurations = configurations;
    }

    @Override
    public void execute() throws Exception {

        Map<String, Object> resultMap = livyCommandProcess.post2LivyWithRetry(buildCommand());
        this.processResult = new ProcessResult();

        Object id = resultMap.get(ID);
        processResult.setProcessId(Integer.valueOf(String.valueOf(id)));
        jobExecutionRequest.setProcessId(Integer.parseInt(String.valueOf(id)));
        LivyStates.State state = LivyStates.toLivyState(resultMap);

        boolean healthy = LivyStates.isHealthy(state);
        if (id != null && healthy) {
            livyCommandProcess.processResultOfLivyState(id, processResult);
        } else {
            processResult.setExitStatusCode(ExecutionStatus.FAILURE.getCode());
        }

        logger.info("process result: " + JSONUtils.toJsonString(this.processResult));

    }

    @Override
    public void after() throws Exception {

    }

    @Override
    public boolean isCancel() throws Exception {
        return this.cancel;
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
    protected String buildCommand() {

        SparkParameters param = JSONUtils.parseObject(jobExecutionRequest.getEngineParameter(), SparkParameters.class);
        assert param != null;

        param.setMainClass("io.datavines.engine.spark.core.SparkDataVinesBootstrap");

        LivySparkParameters parameters = new LivySparkParameters();

        checkSet(param, parameters);

        String jarLibPath = configurations.getString("livy.task.jar.lib.path", "hdfs:///datavines/lib");
        String jarName = configurations.getString("data.quality.jar.name");
        if (jarName.startsWith(SLASH_LIB)) {
            jarName = jarName.replace(SLASH_LIB, "");
        }
        parameters.setFile(jarLibPath + jarName);

        String taskJars = configurations.getString("livy.task.jars");

        List<String> jarsList = new ArrayList<>();
        if (StringUtils.isNotEmpty(taskJars)) {
            List<String> jarList = splitToList(jarLibPath, taskJars);
            jarsList.addAll(jarList);
        } else {
            String jars = param.getJars();
            if (jars.trim().contains(BAR_JARS)) {
                jars = jars.replace(BAR_JARS, "");
                List<String> jarList = splitToList(jarLibPath, jars);
                jarsList.addAll(jarList);
            }
        }

        logger.info("spark engine jars : {}", JSONUtils.toJsonString(jarsList));
        parameters.setJars(jarsList);

        DataVinesJobConfig jobConfig = JSONUtils.parseObject(jobExecutionRequest.getApplicationParameter(), DataVinesJobConfig.class);

        String env = jobExecutionRequest.getEnv();
        Map<String, Object> envMap = parseConfMap(env);
        if (jobConfig.getEnvConfig() != null) {
            EnvConfig envConfig = jobConfig.getEnvConfig();
            envConfig.setConfig(envMap);
        }

        String jsonString = JSONUtils.toJsonString(jobConfig);
        List<String> jobConfigList = Collections.singletonList(jsonString);
        parameters.setArgs(jobConfigList);

        String others = param.getOthers();
        if (StringUtils.isNotEmpty(others)) {
            others = others.replace(BAR_CONF, "");
            Map<String, Object> parseConfMap = parseConfMap(others);
            parameters.setConf(parseConfMap);
        }

        String sparkCommand = JSONUtils.toJsonString(parameters);

        logger.info("data quality task command: {}", sparkCommand);

        return sparkCommand;
    }

    private List<String> splitToList(String jarLibPath, String taskJars) {
        return Arrays.stream(taskJars.split(","))
                .map(jar -> jarLibPath + "/" + jar)
                .collect(Collectors.toList());
    }

    private Map<String, Object> parseConfMap(String conf) {

        if (StringUtils.isNotEmpty(conf)) {
            String[] splitPair = conf.split(",");

            Map<String, Object> confMap = new HashMap<>();
            for (String pair : splitPair) {
                String[] split = pair.split("=");
                if (split.length == 2) {
                    String key = split[0].trim();
                    String value = split[1].trim();
                    confMap.put(key, value);
                }
            }
            return confMap;
        }

        return null;
    }

    private void checkSet(SparkParameters param, LivySparkParameters parameters) {
        ProgramType programType = param.getProgramType();

        String mainClass = param.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            parameters.setClassName(mainClass);
        }

        int driverCores = param.getDriverCores();
        if (driverCores > 0) {
            parameters.setDriverCores(driverCores);
        }

        String driverMemory = param.getDriverMemory();
        if (StringUtils.isNotEmpty(driverMemory)) {
            parameters.setDriverMemory(driverMemory);
        }

        int numExecutors = param.getNumExecutors();
        if (numExecutors > 0) {
            parameters.setNumExecutors(numExecutors);
        }

        int executorCores = param.getExecutorCores();
        if (executorCores > 0) {
            parameters.setExecutorCores(executorCores);
        }

        String executorMemory = param.getExecutorMemory();
        if (StringUtils.isNotEmpty(executorMemory)) {
            parameters.setExecutorMemory(executorMemory);
        }

        String appName = param.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            parameters.setName(appName);
        }

        String queue = param.getQueue();
        if (StringUtils.isNotEmpty(queue)) {
            parameters.setQueue(queue);
        } else {
            parameters.setQueue(DEFAULT);
        }

        String proxyUser = configurations.getString("livy.task.proxyUser");
        if (!StringUtils.isEmpty(proxyUser)) {
            parameters.setProxyUser(proxyUser);
        }
    }
}
