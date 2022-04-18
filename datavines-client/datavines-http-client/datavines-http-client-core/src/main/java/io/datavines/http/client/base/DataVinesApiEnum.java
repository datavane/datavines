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
package io.datavines.http.client.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

public enum DataVinesApiEnum {

    //TASK
    TASK_SUBMIT_API( new DataVinesAPI("/api/v1/task/submit", HttpMethod.POST, Response.Status.OK)),
    TASK_KILL_API( new DataVinesAPI("/api/v1/task/kill/%s", HttpMethod.DELETE, Response.Status.OK)),
    TASK_STATUS_API( new DataVinesAPI("/api/v1/task/status/%s", HttpMethod.GET, Response.Status.OK)),
    TASK_RESULT_API( new DataVinesAPI("/api/v1/task/result/%s", HttpMethod.GET, Response.Status.OK)),
    //METRIC
    METRIC_LIST_API(new DataVinesAPI("/api/v1/metric/list", HttpMethod.GET, Response.Status.OK)),
    METRIC_INFO_API(new DataVinesAPI("/api/v1/metric/info/%s", HttpMethod.GET, Response.Status.OK))
    ;
    private Log log = LogFactory.getLog(DatavinesBaseClient.class);


    private DataVinesAPI dataVinesApi;

    DataVinesApiEnum (DataVinesAPI dataVinesApi){
        this.dataVinesApi = dataVinesApi;
    }

    public DataVinesAPI getDataVinesApi(String ... path){
        String formattedPath = String.format(dataVinesApi.getPath(), path);
        return new DataVinesAPI(formattedPath, dataVinesApi.getMethod(), dataVinesApi.getExpectStatus());
    }

    public DataVinesAPI getDataVinesApi(){
        if (dataVinesApi.getPath().contains("%s")){
            log.error(String.format("%s must be init with path", dataVinesApi.getPath()));
        }
        return dataVinesApi;
    }
}
