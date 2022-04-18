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
package io.datavines.http.clinet;


import io.datavines.http.client.DataVinesClient;
import io.datavines.http.client.base.DatavinesApiException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class DataVinesClientTest {

    private DataVinesClient client;


    @Before
    public void initClient(){
        client = new DataVinesClient("http://localhost:5600");
    }


    @Test
    public void submitTask() throws DatavinesApiException {
        String json = "{\"name\":\"test\",\"parameter\":{\"metricType\":\"column_length\",\"metricParameter\":{\"table\":\"task\",\"column\":\"parameter\",\"comparator\":\">\",\"length\":1},\"srcConnectorParameter\":{\"type\":\"postgresql\",\"parameters\":{\"database\":\"datavines\",\"password\":\"lwslws\",\"port\":\"5432\",\"host\":\"localhost\",\"user\":\"postgres\",\"properties\":\"useUnicode=true&characterEncoding=UTF-8\"}}}}";
        HashMap task = client.submitTask(json);
        System.out.println(task);
    }

    @Test
    public void taskStatus() throws DatavinesApiException{
        HashMap hashMap = client.taskStatus(1516045488414031873L);
        System.out.println(hashMap);
    }

    @Test
    public void taskResultInfo() throws DatavinesApiException{
        HashMap hashMap = client.taskResultInfo(1516045488414031873L);
        System.out.println(hashMap);
    }

    @Test
    public void killTask() throws DatavinesApiException{
        HashMap hashMap = client.killTask(1516045488414031873L);
        System.out.println(hashMap);
    }

    @Test
    public void metricInfo() throws DatavinesApiException{
        HashMap hashMap = client.metricInfo("column_length");
        System.out.println(hashMap);
    }

    @Test
    public void metricList() throws DatavinesApiException{
        HashMap hashMap = client.metricList();
        System.out.println(hashMap);
    }


}
