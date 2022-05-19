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
import java.util.Set;

import io.datavines.common.config.CheckResult;
import io.datavines.common.dto.job.JobCreate;
import io.datavines.common.entity.ConnectorParameter;
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.JSONUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.engine.config.DataQualityConfigurationBuilder;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.ResultFormula;
import io.datavines.metric.api.SqlMetric;
import io.datavines.server.coordinator.repository.entity.Command;
import io.datavines.server.coordinator.repository.entity.Task;
import io.datavines.server.coordinator.repository.mapper.CommandMapper;
import io.datavines.server.coordinator.repository.mapper.TaskMapper;
import io.datavines.server.enums.CommandType;
import io.datavines.server.enums.JobType;
import io.datavines.server.enums.Priority;
import io.datavines.server.exception.DataVinesServerException;
import io.datavines.spi.PluginLoader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.server.coordinator.repository.mapper.JobMapper;
import io.datavines.server.coordinator.repository.service.JobService;
import io.datavines.server.coordinator.repository.entity.Job;

import static io.datavines.server.DataVinesConstants.JDBC;

@Service("jobService")
public class JobServiceImpl extends ServiceImpl<JobMapper,Job> implements JobService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Override
    public long insert(Job job) {
        baseMapper.insert(job);
        return job.getId();
    }

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
    public long createJob(JobCreate jobCreate) throws DataVinesServerException{

        Job job = new Job();
        BeanUtils.copyProperties(jobCreate, job);
        job.setParameter(JSONUtils.toJsonString(jobCreate.getParameter()));

        job.setType(JobType.valueOf(jobCreate.getType()));
        job.setRetryTimes(10000);
        job.setRetryInterval(60000);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());

        // add a job
        long jobId = insert(job);

        // whether running now
        if(jobCreate.getRunningNow() == 1) {

            checkTaskParameter(jobCreate);
            // add a task
            Task task = new Task();
            BeanUtils.copyProperties(jobCreate, task);
            task.setParameter(JSONUtils.toJsonString(jobCreate.getParameter()));

            task.setName(jobCreate.getName() + "_task_" + System.currentTimeMillis());
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
        }
        return jobId;
    }


    private void checkTaskParameter(JobCreate jobCreate) throws DataVinesServerException {
        TaskParameter taskParameter = jobCreate.getParameter();
        String engineType = jobCreate.getEngineType();

        String metricType = taskParameter.getMetricType();
        Set<String> metricPluginSet = PluginLoader.getPluginLoader(SqlMetric.class).getSupportedPlugins();
        if (!metricPluginSet.contains(metricType)) {
            throw new DataVinesServerException(String.format("%s metric does not supported", metricType));
        }

        SqlMetric sqlMetric = PluginLoader.getPluginLoader(SqlMetric.class).getOrCreatePlugin(metricType);
        CheckResult checkResult = sqlMetric.validateConfig(taskParameter.getMetricParameter());
        if (checkResult== null || !checkResult.isSuccess()) {
            throw new DataVinesServerException(checkResult== null? "check error": checkResult.getMsg());
        }

        String configBuilder = engineType + "_" + sqlMetric.getType().getDescription();
        Set<String> configBuilderPluginSet = PluginLoader.getPluginLoader(DataQualityConfigurationBuilder.class).getSupportedPlugins();
        if (!configBuilderPluginSet.contains(configBuilder)) {
            throw new DataVinesServerException(String.format("%s engine does not supported %s metric", engineType, metricType));
        }

        ConnectorParameter srcConnectorParameter = taskParameter.getSrcConnectorParameter();
        if (srcConnectorParameter != null) {
            String srcConnectorType = srcConnectorParameter.getType();
            Set<String> connectorFactoryPluginSet =
                    PluginLoader.getPluginLoader(ConnectorFactory.class).getSupportedPlugins();
            if (!connectorFactoryPluginSet.contains(srcConnectorType)) {
                throw new DataVinesServerException(String.format("%s connector does not supported", srcConnectorType));
            }

            if (JDBC.equals(engineType)) {
                ConnectorFactory srcConnectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(srcConnectorType);
                if (!JDBC.equals(srcConnectorFactory.getCategory())) {
                    throw new DataVinesServerException(String.format("jdbc engine does not supported %s connector", srcConnectorType));
                }
            }
        } else {
            throw new DataVinesServerException("src connector parameter should not be null");
        }

        String expectedMetric = taskParameter.getExpectedType();
        Set<String> expectedValuePluginSet = PluginLoader.getPluginLoader(ExpectedValue.class).getSupportedPlugins();
        if (!expectedValuePluginSet.contains(expectedMetric)) {
            throw new DataVinesServerException(String.format("%s expected value does not supported", metricType));
        }

        String resultFormula = taskParameter.getResultFormula();
        Set<String> resultFormulaPluginSet = PluginLoader.getPluginLoader(ResultFormula.class).getSupportedPlugins();
        if (!resultFormulaPluginSet.contains(resultFormula)) {
            throw new DataVinesServerException(String.format("%s result formula does not supported", metricType));
        }
    }
}
