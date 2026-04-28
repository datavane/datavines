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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.datavines.server.repository.entity.JobQualityReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobQualityReportMapper extends BaseMapper<JobQualityReport>  {

    @Select(value = "SELECT datasource_id, database_name, table_name, '--' as column_name, avg(score) as score, CAST(#{reportDate} AS DATE) as report_date, 'table' as entity_level" +
            " from dv_job_quality_report where report_date = CAST(#{reportDate} AS DATE) and datasource_id = #{datasourceId} and entity_level = 'column'" +
            " group by datasource_id, database_name, table_name", databaseId = "postgresql")
    @Select(value = "SELECT datasource_id, database_name, table_name, '--' as column_name, avg(score) as score, #{reportDate} as report_date, 'table' as entity_level" +
            " from dv_job_quality_report where report_date = #{reportDate} and datasource_id = #{datasourceId} and entity_level = 'column'" +
            " group by datasource_id, database_name, table_name", databaseId = "mysql")
    @Select("SELECT datasource_id, database_name, table_name, '--' as column_name, avg(score) as score, #{reportDate} as report_date, 'table' as entity_level" +
            " from dv_job_quality_report where report_date = #{reportDate} and datasource_id = #{datasourceId} and entity_level = 'column'" +
            " group by datasource_id, database_name, table_name")
    List<JobQualityReport> listTableScoreGroupByDatasource(@Param("datasourceId") Long datasourceId,
                                                   @Param("reportDate") String reportDate);

    @Select(value = "SELECT datasource_id, database_name, '--' as table_name, '--' as column_name, avg(score) as score, CAST(#{reportDate} AS DATE) as report_date, 'database' as entity_level" +
            " from dv_job_quality_report where report_date = CAST(#{reportDate} AS DATE) and datasource_id = #{datasourceId} and entity_level = 'table'" +
            " group by datasource_id, database_name", databaseId = "postgresql")
    @Select(value = "SELECT datasource_id, database_name, '--' as table_name, '--' as column_name, avg(score) as score, #{reportDate} as report_date, 'database' as entity_level" +
            " from dv_job_quality_report where report_date = #{reportDate} and datasource_id = #{datasourceId} and entity_level = 'table'" +
            " group by datasource_id, database_name", databaseId = "mysql")
    @Select("SELECT datasource_id, database_name, '--' as table_name, '--' as column_name, avg(score) as score, #{reportDate} as report_date, 'database' as entity_level" +
            " from dv_job_quality_report where report_date = #{reportDate} and datasource_id = #{datasourceId} and entity_level = 'table'" +
            " group by datasource_id, database_name")
    List<JobQualityReport> listDbScoreGroupByDatasource(@Param("datasourceId") Long datasourceId,
                                                             @Param("reportDate") String reportDate);

    @Select(value = "SELECT datasource_id, '--' as database_name, '--' as table_name, '--' as column_name, avg(score) as score, CAST(#{reportDate} AS DATE) as report_date, 'datasource' as entity_level" +
            " from dv_job_quality_report where report_date = CAST(#{reportDate} AS DATE) and datasource_id = #{datasourceId} and entity_level = 'database'" +
            " group by datasource_id", databaseId = "postgresql")
    @Select(value = "SELECT datasource_id, '--' as database_name, '--' as table_name, '--' as column_name, avg(score) as score, #{reportDate} as report_date, 'datasource' as entity_level" +
            " from dv_job_quality_report where report_date = #{reportDate} and datasource_id = #{datasourceId} and entity_level = 'database'" +
            " group by datasource_id", databaseId = "mysql")
    @Select("SELECT datasource_id, '--' as database_name, '--' as table_name, '--' as column_name, avg(score) as score, #{reportDate} as report_date, 'datasource' as entity_level" +
            " from dv_job_quality_report where report_date = #{reportDate} and datasource_id = #{datasourceId} and entity_level = 'database'" +
            " group by datasource_id")
    List<JobQualityReport> listDatasourceScoreGroupByDatasource(@Param("datasourceId") Long datasourceId,
                                                        @Param("reportDate") String reportDate);

    List<JobQualityReport> listScoreByCondition(@Param("datasourceId") Long datasourceId,
                                                @Param("entityLevel") String entityLevel,
                                                @Param("schemaName") String schemaName,
                                                @Param("tableName") String tableName,
                                                @Param("reportDate") String reportDate);

    List<JobQualityReport> listScoreTrendByCondition(@Param("datasourceId") Long datasourceId,
                                                     @Param("entityLevel") String entityLevel,
                                                     @Param("schemaName") String schemaName,
                                                     @Param("tableName") String tableName,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);

    IPage<JobQualityReport> getQualityReportPage(Page<JobQualityReport> page,
                                                 @Param("datasourceId") Long datasourceId,
                                                 @Param("schemaName") String schemaName,
                                                 @Param("tableName") String tableName,
                                                 @Param("reportDate") String reportDate,
                                                 @Param("entityLevel") String entityLevel);
}
