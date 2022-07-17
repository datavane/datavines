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
package io.datavines.common.entity;

import lombok.Data;

@Data
public class TaskInfo {

    private Long id;
    private String name;
    private String engineType;
    private String engineParameter;
    private String errorDataStorageType;
    private String errorDataStorageParameter;
    private String errorDataFileName;
    private TaskParameter taskParameter;

    public TaskInfo() {
    }

    public TaskInfo(Long id, String name,
                    String engineType, String engineParameter,
                    String errorDataStorageType, String errorDataStorageParameter,String errorDataFileName,
                    TaskParameter taskParameter) {
        this.id = id;
        this.name = name;
        this.engineType = engineType;
        this.engineParameter = engineParameter;
        this.errorDataStorageType = errorDataStorageType;
        this.errorDataStorageParameter = errorDataStorageParameter;
        this.errorDataFileName = errorDataFileName;
        this.taskParameter = taskParameter;
    }
}
