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

package io.datavines.server.coordinator.repository.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.datavines.common.config.CheckResult;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.entity.job.BaseJobParameter;
import io.datavines.common.entity.job.builder.TaskParameterBuilderFactory;
import io.datavines.common.utils.StringUtils;
import io.datavines.server.coordinator.api.entity.dto.job.JobCreate;
import io.datavines.common.entity.ConnectorParameter;
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.JSONUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.config.DataQualityConfigurationBuilder;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.ResultFormula;
import io.datavines.metric.api.SqlMetric;
import io.datavines.server.coordinator.api.enums.ApiStatus;
import io.datavines.server.coordinator.repository.entity.Command;
import io.datavines.server.coordinator.repository.entity.DataSource;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.server.coordinator.repository.mapper.CommandMapper;
import io.datavines.server.coordinator.repository.mapper.DataSourceMapper;
import io.datavines.server.coordinator.repository.mapper.TaskMapper;
import io.datavines.server.enums.CommandType;
import io.datavines.common.enums.JobType;
import io.datavines.server.enums.Priority;
import io.datavines.server.exception.DataVinesServerException;
import io.datavines.server.utils.ContextHolder;
import io.datavines.spi.PluginLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.server.coordinator.repository.mapper.JobMapper;
import io.datavines.server.coordinator.repository.service.JobService;
import io.datavines.server.coordinator.repository.entity.Job;
import org.springframework.transaction.annotation.Transactional;

import static io.datavines.server.DataVinesConstants.JDBC;

@Service("jobService")
public class JobServiceImpl extends ServiceImpl<JobMapper,Job> implements JobService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Override
    public int update(Job job) {
        return baseMapper.updateById(job);
    }

    @Override
    public Job getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<Job> listByDataSourceId(Long dataSourceId) {
        return baseMapper.listByDataSourceId(dataSourceId);
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createJob(JobCreate jobCreate) throws DataVinesServerException{

        String parameter = jobCreate.getParameter();
        if (StringUtils.isEmpty(parameter)) {
            throw new DataVinesServerException(ApiStatus.JOB_PARAMETER_IS_NULL_ERROR);
        }

        Job job = new Job();
        BeanUtils.copyProperties(jobCreate, job);

        job.setName(getJobName(jobCreate.getType(), jobCreate.getParameter()));
        job.setType(JobType.of(jobCreate.getType()));
        job.setCreateBy(ContextHolder.getUserId());
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateBy(ContextHolder.getUserId());
        job.setUpdateTime(LocalDateTime.now());

        // add a job
        baseMapper.insert(job);
        long jobId = job.getId();

        // whether running now
        if(jobCreate.getRunningNow() == 1) {
            DataSource dataSource = dataSourceMapper.selectById(jobCreate.getDataSourceId());
            Map<String, Object> srcSourceConfigMap = JSONUtils.toMap(dataSource.getParam(), String.class, Object.class);
            ConnectionInfo srcConnectionInfo = new ConnectionInfo();
            srcConnectionInfo.setType(dataSource.getType());
            srcConnectionInfo.setConfig(srcSourceConfigMap);

            List<String> taskParameterList = buildTaskParameter(
                    jobCreate.getType(), jobCreate.getParameter(), srcConnectionInfo, null);

            taskParameterList.forEach(param -> {
                // add a task
                job.setId(null);
                Task task = new Task();
                BeanUtils.copyProperties(job, task);
                task.setParameter(param);
                task.setName(job.getName() + "_task_" + System.currentTimeMillis());
                task.setJobId(jobId);
                task.setJobType(job.getType());
                task.setStatus(ExecutionStatus.SUBMITTED_SUCCESS);
                task.setSubmitTime(LocalDateTime.now());
                task.setCreateTime(LocalDateTime.now());
                task.setUpdateTime(LocalDateTime.now());

                taskMapper.insert(task);

                // add a command
                Command command = new Command();
                command.setType(CommandType.START);
                command.setPriority(Priority.MEDIUM);
                command.setTaskId(task.getId());
                commandMapper.insert(command);
            });

        }

        return jobId;
    }

    @Override
    public boolean executeJob(Long jobId) throws DataVinesServerException {
        return false;
    }

    private String getJobName(String jobType, String parameter) {
        List<BaseJobParameter> jobParameters = JSONUtils.toList(parameter, BaseJobParameter.class);

        if (CollectionUtils.isEmpty(jobParameters)) {
            throw new DataVinesServerException(ApiStatus.JOB_PARAMETER_IS_NULL_ERROR);
        }

        BaseJobParameter baseJobParameter = jobParameters.get(0);
        Map<String,Object> metricParameter = baseJobParameter.getMetricParameter();
        if (MapUtils.isEmpty(metricParameter)) {
            throw new DataVinesServerException(ApiStatus.JOB_PARAMETER_IS_NULL_ERROR);
        }

        String database = (String)metricParameter.get("database");
        String table = (String)metricParameter.get("table");
        String column = (String)metricParameter.get("column");

        switch (JobType.of(jobType)) {
            case DATA_QUALITY:
                String metric = baseJobParameter.getMetricType();
                return String.format("%s[%s.%s.%s]%s", metric.toUpperCase(), database, table, column, System.currentTimeMillis());
            case DATA_PROFILE:
                return String.format("%s[%s.%s.%s]%s", "DATA_PROFILE", database, table, column, System.currentTimeMillis());
            default:
                return String.format("%s[%s.%s.%s]%s", "JOB", database, table, column, System.currentTimeMillis());
        }
    }

    private List<String> buildTaskParameter(String jobType, String parameter, ConnectionInfo srcConnectionInfo, ConnectionInfo targetConnectionInfo) {
        return TaskParameterBuilderFactory.builder(JobType.of(jobType))
                .buildTaskParameter(parameter,srcConnectionInfo,targetConnectionInfo);
    }
}
