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
package io.datavines.server.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.server.api.dto.vo.JobExecutionResultVO;
import io.datavines.server.repository.entity.JobExecutionResult;

import java.util.List;

public interface JobExecutionResultService extends IService<JobExecutionResult> {

    long insert(JobExecutionResult jobExecutionResult);

    int update(JobExecutionResult jobExecutionResult);

    int deleteByJobExecutionId(long taskId);

    JobExecutionResult getById(long id);

    JobExecutionResult getByJobExecutionId(long taskId);

    JobExecutionResultVO getResultVOByJobExecutionId(long taskId);

    List<JobExecutionResult> listByJobIdAndTimeRange(Long jobId, String startTime, String endTime);
}
