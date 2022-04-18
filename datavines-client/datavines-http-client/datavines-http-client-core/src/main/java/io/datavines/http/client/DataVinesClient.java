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
package io.datavines.http.client;

import io.datavines.http.client.base.DataVinesApiEnum;
import io.datavines.http.client.base.DatavinesApiException;
import io.datavines.http.client.base.DatavinesBaseClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;

public class DataVinesClient extends DatavinesBaseClient {

    private Log log = LogFactory.getLog(DataVinesClient.class);

    public DataVinesClient(String baseUrl){
        super(baseUrl, new Properties(), "test", null);
    }

    /**
     * submit task
     * @param params
     * @return
     */
    public HashMap submitTask(String params) throws DatavinesApiException {
        HashMap res = callAPI(DataVinesApiEnum.TASK_SUBMIT_API.getDataVinesApi(),  service, params);
        return res;
    }

    /**
     * kill task
     * @param taskId
     * @return
     * @throws DatavinesApiException
     */
    public HashMap killTask(Long taskId) throws DatavinesApiException {
       return callApiWithPathParam(DataVinesApiEnum.TASK_KILL_API, taskId);
    }

    /**
     * get task status
     * @param taskId
     * @return task status
     * @throws DatavinesApiException
     */
    public HashMap taskStatus(Long taskId) throws DatavinesApiException{
        return callApiWithPathParam(DataVinesApiEnum.TASK_STATUS_API, taskId);
    }

    /**
     * get task result info
     * @param taskId
     * @return task result
     * @throws DatavinesApiException
     */
    public HashMap taskResultInfo(Long taskId) throws DatavinesApiException{
        return callApiWithPathParam(DataVinesApiEnum.TASK_RESULT_API, taskId);
    }

    /**
     * get metric list
     * @return metric list
     * @throws DatavinesApiException
     */
    public HashMap metricList() throws DatavinesApiException {
        return callApi(DataVinesApiEnum.METRIC_LIST_API);
    }

    /**
     * get metric info by metric name
     * @param name metric name
     * @return metric info
     * @throws DatavinesApiException
     */
    public HashMap metricInfo(String name) throws DatavinesApiException {
        return callApiWithPathParam(DataVinesApiEnum.METRIC_INFO_API, name);
    }



    private HashMap callApiWithPathParam(DataVinesApiEnum dataVinesAPI, Serializable id) throws DatavinesApiException{
        if (Objects.isNull(id)){
            log.error("task id must not null!");
            throw new DatavinesApiException("");
        }
        HashMap res = callAPI(dataVinesAPI.getDataVinesApi(String.valueOf(id)),  service, null);
        return res;
    }

    private HashMap callApi(DataVinesApiEnum dataVinesAPI) throws DatavinesApiException{
        HashMap res = callAPI(dataVinesAPI.getDataVinesApi(),  service, null);
        return res;
    }
}
