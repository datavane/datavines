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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.entity.job.BaseJobParameter;
import io.datavines.common.entity.job.builder.TaskParameterBuilderFactory;
import io.datavines.common.utils.StringUtils;
import io.datavines.server.coordinator.api.entity.dto.job.JobCreate;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.JSONUtils;
import io.datavines.server.coordinator.api.entity.dto.job.JobUpdate;
import io.datavines.server.coordinator.api.entity.vo.JobVO;
import io.datavines.server.coordinator.api.enums.ApiStatus;
import io.datavines.server.coordinator.repository.entity.*;
import io.datavines.server.coordinator.repository.mapper.*;
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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.server.coordinator.repository.service.JobService;

import org.springframework.transaction.annotation.Transactional;

import io.datavines.server.coordinator.repository.entity.Job;


@Slf4j
@Service("jobService")
public class JobServiceImpl extends ServiceImpl<JobMapper,Job> implements JobService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private EnvMapper envMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Override
    public int update(JobUpdate jobUpdate) {
        Job job = getById(jobUpdate.getId());
        if ( job == null) {
            throw new DataVinesServerException(ApiStatus.JOB_NOT_EXIST_ERROR, jobUpdate.getId());
        }

        BeanUtils.copyProperties(jobUpdate, job);
        job.setName(getJobName(jobUpdate.getType(), jobUpdate.getParameter()));
        job.setUpdateBy(ContextHolder.getUserId());
        job.setUpdateTime(LocalDateTime.now());

        if (baseMapper.updateById(job) <= 0) {
            log.info("update workspace fail : {}", jobUpdate);
            throw new DataVinesServerException(ApiStatus.UPDATE_JOB_ERROR, job.getName());
        }

        return 1;
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

    public long create(JobCreate jobCreate) throws DataVinesServerException{

        // 需要对参数进行校验，判断插件类型是否存在
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
            executeJob(job);
        }

        return jobId;
    }

    @Override
    public IPage<JobVO> getJobPage(String searchVal, Long dataSourceId, Integer pageNumber, Integer pageSize) {
        Page<JobVO> page = new Page<>(pageNumber, pageSize);
        IPage<JobVO> jobs = baseMapper.getJobPage(page, searchVal, dataSourceId);
        return jobs;
    }

    @Override
    public boolean execute(Long jobId) throws DataVinesServerException {
        Job job = baseMapper.selectById(jobId);
        if  (job == null) {
            throw new DataVinesServerException(ApiStatus.JOB_NOT_EXIST_ERROR, jobId);
        }

        executeJob(job);

        return true;
    }

    private void executeJob(Job job) {
        DataSource dataSource = dataSourceMapper.selectById(job.getDataSourceId());
        Map<String, Object> srcSourceConfigMap = JSONUtils.toMap(dataSource.getParam(), String.class, Object.class);
        ConnectionInfo srcConnectionInfo = new ConnectionInfo();
        srcConnectionInfo.setType(dataSource.getType());
        srcConnectionInfo.setConfig(srcSourceConfigMap);

        List<String> taskParameterList = buildTaskParameter(
                job.getType().getDescription(), job.getParameter(), srcConnectionInfo, null);
        long jobId = job.getId();
        Env env = envMapper.selectById(job.getEnv());
        String envStr = "";
        if (env != null) {
            envStr = env.getEnv();
        }

        Tenant tenant = tenantMapper.selectById(job.getTenantCode());
        String tenantStr = "";
        if (tenant != null) {
            tenantStr = tenant.getTenant();
        }

        for (String param: taskParameterList) {
            // add a task
            job.setId(null);
            Task task = new Task();
            BeanUtils.copyProperties(job, task);
            task.setJobId(jobId);
            task.setParameter(param);
            task.setName(job.getName() + "_task_" + System.currentTimeMillis());
            task.setJobType(job.getType());
            task.setStatus(ExecutionStatus.SUBMITTED_SUCCESS);
            task.setTenantCode(tenantStr);
            task.setEnv(envStr);
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
        }
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
