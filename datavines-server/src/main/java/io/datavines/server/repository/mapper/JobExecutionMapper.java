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
package io.datavines.server.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.datavines.server.api.dto.vo.JobExecutionAggItem;
import io.datavines.server.api.dto.vo.JobExecutionStat;
import io.datavines.server.api.dto.vo.JobExecutionTrendBarItem;
import io.datavines.server.api.dto.vo.JobExecutionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.datavines.server.repository.entity.JobExecution;

@Mapper
public interface JobExecutionMapper extends BaseMapper<JobExecution>  {

    @Select("SELECT * from dv_job_execution WHERE job_id = #{jobId} ")
    List<JobExecution> listByJobId(long jobId);

    IPage<JobExecutionVO> getJobExecutionPage(Page<JobExecutionVO> page,
                                              @Param("searchVal") String searchVal,
                                              @Param("jobId") Long jobId,
                                              @Param("datasourceId") Long datasourceId,
                                              @Param("status") Integer status,
                                              @Param("metricType") String metricType, @Param("schemaName") String schemaName,
                                              @Param("tableName") String tableName, @Param("columnName") String columnName,
                                              @Param("startTime") String startTime, @Param("endTime") String endTime,
                                              @Param("schemaSearch") String schemaSearch,
                                              @Param("tableSearch") String tableSearch,
                                              @Param("columnSearch") String columnSearch);

    List<JobExecutionAggItem> getJobExecutionAggPie(@Param("datasourceId") Long datasourceId,
                                                    @Param("metricType") String metricType, @Param("schemaName") String schemaName,
                                                    @Param("tableName") String tableName, @Param("columnName") String columnName,
                                                    @Param("startTime") String startTime, @Param("endTime") String endTime);
    List<JobExecutionTrendBarItem> getJobExecutionTrendBar(@Param("datasourceId") Long datasourceId,
                                                           @Param("metricType") String metricType, @Param("schemaName") String schemaName,
                                                           @Param("tableName") String tableName, @Param("columnName") String columnName,
                                                           @Param("startTime") String startTime, @Param("endTime") String endTime);


    JobExecutionStat getJobExecutionStat(@Param("jobId") Long jobId);
}
