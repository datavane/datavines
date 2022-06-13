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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.datavines.server.coordinator.api.entity.vo.JobVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.datavines.server.coordinator.repository.entity.Job;

@Mapper
public interface JobMapper extends BaseMapper<Job> {

    @Select("SELECT * from dv_job WHERE datasource_id = #{datasourceId} ")
    List<Job> listByDataSourceId(long dataSourceId);

    IPage<JobVO> getJobPage(Page<JobVO> page,
                                   @Param("searchVal") String searchVal,
                                   @Param("datasourceId") Long datasourceId);
}
