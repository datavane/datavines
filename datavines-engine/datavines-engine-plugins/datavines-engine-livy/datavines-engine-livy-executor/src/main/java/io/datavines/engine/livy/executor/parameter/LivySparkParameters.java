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
package io.datavines.engine.livy.executor.parameter;

import java.util.List;
import java.util.Map;

public class LivySparkParameters {

    /**
     * main class
     */
    private String className;

    /**
     * executor-cores Number of cores per executor
     */
    private Integer executorCores;

    /**
     * driver-memory Memory for driver
     */
    private String driverMemory;

    /**
     * num-executors Number of executors to launch
     */
    private Integer numExecutors;

    /**
     * driver-cores Number of cores used by the driver, only in cluster mode
     */
    private Integer driverCores;

    /**
     * executor-cores Number of cores per executor
     */
    private String executorMemory;

    /**
     * app name
     */
    private String name;

    /**
     * queue
     */
    private String queue;

    /**
     * proxyUser
     */
    private String proxyUser;

    /**
     * main file
     */
    private String file;

    /**
     * arguments
     */
    private List<String> args;

    /**
     * jars
     */
    private List<String> jars;

    /**
     * conf
     */
    private Map<String, Object> conf;


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getExecutorCores() {
        return executorCores;
    }

    public void setExecutorCores(Integer executorCores) {
        this.executorCores = executorCores;
    }

    public String getDriverMemory() {
        return driverMemory;
    }

    public void setDriverMemory(String driverMemory) {
        this.driverMemory = driverMemory;
    }

    public Integer getNumExecutors() {
        return numExecutors;
    }

    public void setNumExecutors(Integer numExecutors) {
        this.numExecutors = numExecutors;
    }

    public Integer getDriverCores() {
        return driverCores;
    }

    public void setDriverCores(Integer driverCores) {
        this.driverCores = driverCores;
    }

    public String getExecutorMemory() {
        return executorMemory;
    }

    public void setExecutorMemory(String executorMemory) {
        this.executorMemory = executorMemory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<String> getJars() {
        return jars;
    }

    public void setJars(List<String> jars) {
        this.jars = jars;
    }

    public Map<String, Object> getConf() {
        return conf;
    }

    public void setConf(Map<String, Object> conf) {
        this.conf = conf;
    }
}
