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
package io.datavines.engine.flink.executor.parameter;

import lombok.Data;

@Data
public class FlinkParameters {

    private String mainJar;

    private String mainClass;

    private String deployMode;

    private String taskManagerCount;

    private String taskManagerMemory;

    private String jobManagerMemory;

    private String mainArgs;

    private String yarnQueue;

    private String jobName;

    private int parallelism = 1;

    private String flinkOthers;

    private String jars;

    private String tags;
}
