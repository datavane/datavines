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

import io.datavines.server.coordinator.repository.entity.Job;

@Mapper
public interface JobMapper extends BaseMapper<Job> {

    @Select("SELECT * from dv_job WHERE datasource_id = #{datasourceId} ")
    List<Job> listByDataSourceId(long dataSourceId);

    @Insert("INSERT INTO dv_job(id, name, type, datasource_id, " +
            "parameter, retry_times, retry_interval, timeout, timeout_strategy, tenant_code, create_by, create_time, update_by, update_time)" +
            "values(#{id}, #{name}, #{type}, #{dataSourceId}, #{parameter}, #{retryTimes}, #{retryInterval}, #{timeout},#{timeoutStrategy}, #{tenantCode}," +
            " #{createBy}, #{createTime}, #{updateBy}, #{updateTime})")
    @Options(keyColumn = "id", keyProperty = "id", useGeneratedKeys = true)
    int insert(Job job);

    @Select("SELECT id, name, type, datasource_id,parameter, retry_times, retry_interval, timeout, " +
            "timeout_strategy, tenant_code, create_by, create_time, update_by, update_time " +
            "FROM dv_job " +
            "WHERE id = #{id}")
    Job getById(long id);

    @Delete("DELETE FROM dv_job where id = #{id}")
    int deleteById(long id);

    @Update("UPDATE dv_job SET name = #{name}, type = #{type}, datasource_id = #{dataSourceId}, " +
            "parameter = #{parameter}, retry_times = #{retryTimes}, retry_interval = #{retryInterval}, timeout = #{timeout}, " +
            "timeout_strategy = #{timeoutStrategy}, tenant_code = #{tenantCode} " +
            "where id = #{id}")
    int update(Job job);
}
