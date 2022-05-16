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
package io.datavines.server.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.datavines.server.coordinator.repository.entity.Task;

@Mapper
public interface TaskMapper extends BaseMapper<Task>  {

    @Select("SELECT * from dv_task WHERE datasource_id = #{dataSourceId} ")
    List<Task> listByDataSourceId(long dataSourceId);

    @Insert("INSERT INTO dv_task (id, name, job_id, job_type, datasource_id, execute_platform_type, execute_platform_parameter, engine_type," +
            "engine_parameter, parameter, status, retry_times, retry_interval, timeout, timeout_strategy, tenant_code, execute_host," +
            "application_id, application_tag, process_id, execute_file_path, log_path, env, submit_time, start_time, end_time, create_time, update_time)" +
            "values(#{id},#{name},#{jobId}, #{jobType}, #{dataSourceId}, #{executePlatformType},#{executePlatformParameter},#{engineType}" +
            ",#{engineParameter},#{parameter},#{status},#{retryTimes},#{retryInterval},#{timeout},#{timeoutStrategy},#{tenantCode}" +
            ",#{executeHost},#{applicationId},#{applicationIdTag},#{processId},#{executeFilePath},#{logPath},#{env},#{submitTime}" +
            ",#{startTime},#{endTime},#{createTime},#{updateTime})")
    @Options(keyColumn = "id", keyProperty = "id", useGeneratedKeys = true)
    int insert(Task task);
}
