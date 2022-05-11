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
package io.datavines.server.coordinator.server.log;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.datavines.common.entity.LogResult;
import io.datavines.common.utils.IOUtils;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.server.coordinator.repository.service.TaskService;

@Component
public class LogService {

    private final Logger logger = LoggerFactory.getLogger(LogService.class);

    @Autowired
    private TaskService taskService;

    public LogResult queryLog(long taskId, int offsetLine){
        return this.queryLog(taskId,offsetLine,10000);
    }

    public LogResult queryLog(long taskId, int offsetLine, int limit){

        Task task = getExecutionJob(taskId);
        if (task == null) {
            return null;
        }

        List<String> contents = readPartFileContent(task.getLogPath(), offsetLine, limit);
        StringBuilder msg = new StringBuilder();

        if (CollectionUtils.isNotEmpty(contents)) {
            for (String line:contents) {
                msg.append(line).append("\r\n");
            }
            offsetLine = offsetLine + contents.size();
        }

        return new LogResult(msg.toString(), offsetLine);
    }

    public LogResult queryWholeLog(long taskId){
        Task task = getExecutionJob(taskId);
        if (task == null) {
            return null;
        }
        return new LogResult(readWholeFileContent(task.getLogPath()),0);
    }

    public byte[] getLogBytes(long taskId){

        Task task = getExecutionJob(taskId);
        if (task == null) {
            return null;
        }
        return getFileContentBytes(task.getLogPath());
    }

    private Task getExecutionJob(long taskId) {
        Task task = taskService.getById(taskId);
        if(task == null || StringUtils.isEmpty(task.getLogPath())){
            logger.info("job {} is not exist",taskId);
            return null;
        }

        return task;
    }

    /**
     * read part file content，can skip any line and read some lines
     *
     * @param filePath file path
     * @param skipLine skip line
     * @param limit read lines limit
     * @return part file content
     */
    private List<String> readPartFileContent(String filePath,
                                             int skipLine,
                                             int limit){
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("read file error",e);
        }
        return Collections.emptyList();
    }

    /**
     * read whole file content
     *
     * @param filePath file path
     * @return whole file content
     */
    private String readWholeFileContent(String filePath) {
        BufferedReader br = null;
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            while ((line = br.readLine()) != null){
                sb.append(line).append("\r\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error("read file error",e);
        } finally {
            IOUtils.closeQuietly(br);
        }
        return "";
    }

    /**
     * get files content bytes，for down load file
     *
     * @param filePath file path
     * @return byte array of file
     * @throws Exception exception
     */
    private byte[] getFileContentBytes(String filePath) {
        InputStream in = null;
        ByteArrayOutputStream bos = null;
        try {
            in = new FileInputStream(filePath);
            bos  = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("get file bytes error",e);
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(in);
        }
        return new byte[0];
    }
}
