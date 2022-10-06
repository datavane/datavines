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
package io.datavines.server.repository.service.impl;

import io.datavines.common.entity.TaskParameter;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.core.utils.LanguageUtils;
import io.datavines.metric.api.ConfigItem;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.ResultFormula;
import io.datavines.metric.api.SqlMetric;
import io.datavines.server.api.dto.vo.TaskResultVO;
import io.datavines.server.repository.entity.Job;
import io.datavines.server.repository.entity.Task;
import io.datavines.server.repository.service.JobService;
import io.datavines.server.repository.service.TaskService;
import io.datavines.server.enums.DqTaskState;
import io.datavines.server.enums.OperatorType;
import io.datavines.server.repository.entity.TaskResult;
import io.datavines.server.repository.mapper.TaskResultMapper;
import io.datavines.server.repository.service.TaskResultService;
import io.datavines.spi.PluginLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.*;

@Service("taskResultService")
public class TaskResultServiceImpl extends ServiceImpl<TaskResultMapper, TaskResult>  implements TaskResultService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private JobService jobService;

    @Override
    public long insert(TaskResult taskResult) {
        baseMapper.insert(taskResult);
        return taskResult.getId();
    }

    @Override
    public int update(TaskResult taskResult) {
        return baseMapper.updateById(taskResult);
    }

    @Override
    public int deleteByTaskId(long taskId) {
        return baseMapper.delete(new QueryWrapper<TaskResult>().eq("task_id",taskId));
    }

    @Override
    public TaskResult getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public TaskResult getByTaskId(long taskId) {
        return baseMapper.selectOne(new QueryWrapper<TaskResult>().eq("task_id", taskId));
    }

    @Override
    public TaskResultVO getResultVOByTaskId(long taskId) {
        TaskResultVO taskResultVO = new TaskResultVO();
        Map<String,String> parameters = new HashMap<>();
        TaskResult taskResult = baseMapper.getOne(taskId);
        parameters.put("actual_value",taskResult.getActualValue()+"");
        parameters.put("expected_value",taskResult.getExpectedValue()+"");
        parameters.put("threshold",taskResult.getThreshold()+"");
        parameters.put("operator",OperatorType.of(taskResult.getOperator()).getSymbol());

        Task task = taskService.getById(taskId);
        if (!Objects.isNull(task)) {
            Job job = jobService.getById(task.getJobId());
            List<TaskParameter> taskParameterList = JSONUtils.toList(job.getParameter(),TaskParameter.class);
            for (TaskParameter taskParameter : taskParameterList) {
                if (taskParameter != null) {
                    SqlMetric sqlMetric = PluginLoader.getPluginLoader(SqlMetric.class).getOrCreatePlugin(taskParameter.getMetricType());
                    Map<String,ConfigItem> configMap = sqlMetric.getConfigMap();
                    Map<String,Object> paramMap = new HashMap<>();
                    String uniqueName = taskParameter.getMetricType() + "."
                            + taskParameter.getMetricParameter().get("database")+ "."
                            + taskParameter.getMetricParameter().get("table")+ "."
                            + taskParameter.getMetricParameter().get("column");

                    String taskResultUniqueName = taskResult.getMetricName()+ "."
                            + taskResult.getDatabaseName() + "."
                            + taskResult.getTableName() + "."
                            + taskResult.getColumnName();

                    if (uniqueName.equalsIgnoreCase(taskResultUniqueName)) {
                        configMap.entrySet().stream().filter(x->{
                            return !("column".equalsIgnoreCase(x.getKey()) || "table".equalsIgnoreCase(x.getKey()) || "filter".equalsIgnoreCase(x.getKey()));
                        }).forEach(config -> {
                            paramMap.put(config.getValue().getLabel(!LanguageUtils.isZhContext()), taskParameter.getMetricParameter().get(config.getKey()));
                        });
                        taskResultVO.setMetricParameter(paramMap);
                    }
                }
            }
        }

        ResultFormula resultFormula =
                PluginLoader.getPluginLoader(ResultFormula.class).getOrCreatePlugin(taskResult.getResultFormula());
        String resultFormulaFormat = resultFormula.getResultFormat(!LanguageUtils.isZhContext())+" ${operator} ${threshold}";

        taskResultVO.setCheckSubject(taskResult.getDatabaseName() + "." + taskResult.getTableName() + "." + taskResult.getColumnName());
        taskResultVO.setCheckResult(DqTaskState.of(taskResult.getState()).getDescription(!LanguageUtils.isZhContext()));
        ExpectedValue expectedValue = PluginLoader.getPluginLoader(ExpectedValue.class).getOrCreatePlugin(task.getEngineType() + "_" + taskResult.getExpectedType());

        taskResultVO.setExpectedType(expectedValue.getNameByLanguage(!LanguageUtils.isZhContext()));
        SqlMetric sqlMetric = PluginLoader.getPluginLoader(SqlMetric.class).getOrCreatePlugin(taskResult.getMetricName());
        taskResultVO.setMetricName(sqlMetric.getNameByLanguage(!LanguageUtils.isZhContext()));
        taskResultVO.setResultFormulaFormat(PlaceholderUtils.replacePlaceholders(resultFormulaFormat, parameters, true));

        return taskResultVO;
    }
}
