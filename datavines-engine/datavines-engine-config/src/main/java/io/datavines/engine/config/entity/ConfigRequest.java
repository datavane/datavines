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
package io.datavines.engine.config.entity;

import io.datavines.common.entity.TaskInfo;
import java.util.Map;

public class ConfigRequest {
    private Map<String, String> inputParameter;
    private TaskInfo taskInfo;

    public ConfigRequest() {
    }

    public ConfigRequest(Map<String, String> inputParameter, TaskInfo taskInfo) {
        this.inputParameter = inputParameter;
        this.taskInfo = taskInfo;
    }

    public Map<String, String> getInputParameter() {
        return inputParameter;
    }

    public void setInputParameter(Map<String, String> inputParameter) {
        this.inputParameter = inputParameter;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }
}
