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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.datavines.common.config.CheckResult;
import io.datavines.common.entity.ConnectorParameter;
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.param.ExecuteRequestParam;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.core.enums.ApiStatus;
import io.datavines.engine.config.DataQualityConfigurationBuilder;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.ResultFormula;
import io.datavines.metric.api.SqlMetric;
import io.datavines.server.coordinator.api.dto.vo.TaskVO;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.coordinator.repository.service.ActualValuesService;
import io.datavines.server.coordinator.repository.service.TaskResultService;
import io.datavines.spi.PluginLoader;
import io.datavines.storage.api.StorageFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.utils.JSONUtils;
import io.datavines.server.coordinator.api.dto.bo.task.SubmitTask;
import io.datavines.server.enums.CommandType;
import io.datavines.server.enums.Priority;
import io.datavines.server.coordinator.repository.entity.Command;
import io.datavines.server.coordinator.repository.mapper.CommandMapper;
import io.datavines.server.coordinator.repository.mapper.TaskMapper;
import io.datavines.server.coordinator.repository.service.TaskService;
import io.datavines.server.coordinator.repository.entity.Task;
import org.springframework.transaction.annotation.Transactional;

import static io.datavines.core.constant.DataVinesConstants.JDBC;
import static io.datavines.core.constant.DataVinesConstants.SPARK;

@Service("taskService")
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>  implements TaskService {

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private TaskResultService taskResultService;

    @Autowired
    private ActualValuesService actualValuesService;

    @Override
    public long create(Task task) {
        baseMapper.insert(task);
        return task.getId();
    }

    @Override
    public int update(Task task) {
        return baseMapper.updateById(task);
    }

    @Override
    public Task getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<Task> listByJobId(long jobId) {
        return baseMapper.listByJobId(jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByJobId(long jobId) {
        List<Task> taskList = listByJobId(jobId);
        if (CollectionUtils.isEmpty(taskList)) {
            return 0;
        }

        taskList.forEach(task -> {
            baseMapper.deleteById(task.getId());
            taskResultService.deleteByTaskId(task.getId());
            actualValuesService.deleteByTaskId(task.getId());
            //删除错误数据存储
        });

        return 0;
    }

    @Override
    public IPage<TaskVO> getTaskPage(String searchVal, Long jobId, Integer pageNumber, Integer pageSize) {
        Page<TaskVO> page = new Page<>(pageNumber, pageSize);
        IPage<TaskVO> jobs = baseMapper.getTaskPage(page, searchVal, jobId);
        return jobs;
    }

    @Override
    public Long submitTask(SubmitTask submitTask) throws DataVinesServerException {

        checkTaskParameter(submitTask.getParameter(), submitTask.getEngineType());

        Task task = new Task();
        BeanUtils.copyProperties(submitTask,task);
        task.setParameter(JSONUtils.toJsonString(submitTask.getParameter()));
        if (submitTask.getExecutePlatformParameter() != null) {
            task.setExecutePlatformParameter(JSONUtils.toJsonString(submitTask.getExecutePlatformParameter()));
        }

        if(SPARK.equals(task.getEngineType())) {
            Map<String,Object> defaultEngineParameter = new HashMap<>();
            defaultEngineParameter.put("programType", "JAVA");
            defaultEngineParameter.put("deployMode", "cluster");
            defaultEngineParameter.put("driverCores", 1);
            defaultEngineParameter.put("driverMemory", "512M");
            defaultEngineParameter.put("numExecutors", 2);
            defaultEngineParameter.put("executorMemory", "2G");
            defaultEngineParameter.put("executorCores", 2);
            defaultEngineParameter.put("others", "--conf spark.yarn.maxAppAttempts=1");

            if (submitTask.getEngineParameter() != null) {
                defaultEngineParameter.putAll(submitTask.getEngineParameter());
            }
            submitTask.setEngineParameter(defaultEngineParameter);
            task.setEngineParameter(JSONUtils.toJsonString(submitTask.getEngineParameter()));
        }

        task.setSubmitTime(LocalDateTime.now());
        task.setStatus(ExecutionStatus.SUBMITTED_SUCCESS);

        return executeTask(task);
    }

    @Override
    public Long executeTask(Task task) throws DataVinesServerException {
        Long taskId = create(task);

        Command command = new Command();
        command.setType(CommandType.START);
        command.setPriority(Priority.MEDIUM);
        command.setTaskId(taskId);
        commandMapper.insert(command);

        return taskId;
    }

    @Override
    public Long killTask(Long taskId) {
        Command command = new Command();
        command.setType(CommandType.STOP);
        command.setPriority(Priority.MEDIUM);
        command.setTaskId(taskId);
        commandMapper.insert(command);

        return taskId;
    }

    @Override
    public List<Task> listNeedFailover(String host) {
        return baseMapper.selectList(new QueryWrapper<Task>()
                .eq("execute_host", host)
                .eq("status",ExecutionStatus.RUNNING_EXECUTION.getCode()));
    }

    @Override
    public List<Task> listTaskNotInServerList(List<String> hostList) {
        return baseMapper.selectList(new QueryWrapper<Task>()
                .in("execute_host", hostList)
                .eq("status",ExecutionStatus.RUNNING_EXECUTION.getCode()));
    }

    private void checkTaskParameter(TaskParameter taskParameter, String engineType) throws DataVinesServerException {
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

        ConnectorParameter connectorParameter = taskParameter.getConnectorParameter();
        if (connectorParameter != null) {
            String connectorType = connectorParameter.getType();
            Set<String> connectorFactoryPluginSet =
                    PluginLoader.getPluginLoader(ConnectorFactory.class).getSupportedPlugins();
            if (!connectorFactoryPluginSet.contains(connectorType)) {
                throw new DataVinesServerException(String.format("%s connector does not supported", connectorType));
            }

            if (JDBC.equals(engineType)) {
                ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(connectorType);
                if (!JDBC.equals(connectorFactory.getCategory())) {
                    throw new DataVinesServerException(String.format("jdbc engine does not supported %s connector", connectorType));
                }
            }
        } else {
            throw new DataVinesServerException("connector parameter should not be null");
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

    @Override
    public Object readErrorDataPage(Long taskId, Integer pageNumber, Integer pageSize)  {

        Task task = getById(taskId);
        if (task == null) {
            throw new DataVinesServerException(ApiStatus.TASK_NOT_EXIST_ERROR, taskId);
        }

        String errorDataStorageType = task.getErrorDataStorageType();
        String errorDataStorageParameter = task.getErrorDataStorageParameter();
        String errorDataFileName = task.getErrorDataFileName();

        StorageFactory storageFactory =
                PluginLoader.getPluginLoader(StorageFactory.class).getOrCreatePlugin(errorDataStorageType);

        ExecuteRequestParam param = new ExecuteRequestParam();
        param.setType(errorDataStorageType);
        param.setDataSourceParam(errorDataStorageParameter);
        param.setScript(errorDataFileName);
        param.setPageNumber(pageNumber);
        param.setPageSize(pageSize);

        Object result = null;
        try {
            result = storageFactory.getStorageExecutor().executeSyncQuery(param).getResult();
        } catch (Exception exception) {
            throw new DataVinesException(exception);
        }

        return result;
    }

    /**
     * get task host from taskId
     * @param taskId
     * @return
     * @throws DataVinesServerException
     */
    @Override
    public String getTaskExecuteHost(Long taskId) {
        Task task = baseMapper.selectById(taskId);
        if(null == task){
            throw new DataVinesServerException(ApiStatus.TASK_NOT_EXIST_ERROR, taskId);
        }
        String executeHost = task.getExecuteHost();
        if(StringUtils.isEmpty(executeHost)){
            throw new DataVinesServerException(ApiStatus.TASK_EXECUTE_HOST_NOT_EXIST_ERROR, taskId);
        }
        return executeHost;
    }
}
