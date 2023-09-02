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

import io.datavines.common.param.ExecuteRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.connector.api.ConnectorFactory;
import io.datavines.core.enums.Status;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.repository.entity.DataSource;
import io.datavines.server.repository.entity.JobExecution;
import io.datavines.server.repository.mapper.JobExecutionMapper;
import io.datavines.server.repository.service.DataSourceService;
import io.datavines.server.repository.service.JobExecutionErrorDataService;
import io.datavines.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static io.datavines.common.ConfigConstants.ERROR_DATA_OUTPUT_TO_DATASOURCE_DATABASE;

@Slf4j
@Service("jobExecutionErrorDataService")
public class JobExecutionErrorDataServiceImpl implements JobExecutionErrorDataService {

    @Autowired
    private JobExecutionMapper jobExecutionMapper;

    @Autowired
    private DataSourceService dataSourceService;

    @Override
    public Object readErrorDataPage(Long jobExecutionId, Integer pageNumber, Integer pageSize)  {

        JobExecution jobExecution = jobExecutionMapper.selectById(jobExecutionId);
        if (jobExecution == null) {
            throw new DataVinesServerException(Status.TASK_NOT_EXIST_ERROR, jobExecutionId);
        }

        String errorDataStorageType = jobExecution.getErrorDataStorageType();
        String errorDataStorageParameter = jobExecution.getErrorDataStorageParameter();
        String errorDataFileName = jobExecution.getErrorDataFileName();

        if (StringUtils.isEmpty(errorDataStorageType) ||
                StringUtils.isEmpty(errorDataStorageParameter) ||
                StringUtils.isEmpty(errorDataFileName)) {
            return null;
        }

        ConnectorFactory connectorFactory =
                PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(errorDataStorageType);

        ExecuteRequestParam param = new ExecuteRequestParam();
        param.setType(errorDataStorageType);
        param.setDataSourceParam(errorDataStorageParameter);

        Map<String,String> errorDataStorageParamMap = JSONUtils.toMap(errorDataStorageParameter);
        Map<String,String> scriptConfigMap = new HashMap<>();
        if (StringUtils.isNotEmpty(errorDataStorageParamMap.get(ERROR_DATA_OUTPUT_TO_DATASOURCE_DATABASE))) {
            scriptConfigMap.put(ERROR_DATA_OUTPUT_TO_DATASOURCE_DATABASE,errorDataStorageParamMap.get(ERROR_DATA_OUTPUT_TO_DATASOURCE_DATABASE));
            DataSource dataSource = dataSourceService.getDataSourceById(jobExecution.getDataSourceId());
            param.setDataSourceParam(dataSource.getParam());
        }

        scriptConfigMap.put("error_data_file_name", errorDataFileName);

        param.setScript(connectorFactory.getDialect().getErrorDataScript(scriptConfigMap));
        param.setPageNumber(pageNumber);
        param.setPageSize(pageSize);

        Object result = null;
        try {
            result = connectorFactory.getExecutor().queryForPage(param).getResult();
        } catch (Exception exception) {
            log.error("read error-data error: ", exception);
        }

        return result;
    }
}
