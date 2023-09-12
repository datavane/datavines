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

package io.datavines.engine.executor.core.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import io.datavines.common.config.Configurations;
import io.datavines.common.utils.JSONUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


public class LivyTaskSubmitHelper {

    private static final Logger logger = LoggerFactory.getLogger(LivyTaskSubmitHelper.class);

    private static final String REQUEST_BY_HEADER = "X-Requested-By";

    private static final int SLEEP_TIME = 1000;

    private RestTemplate restTemplate = new RestTemplate();

    private String uri;

    private Configurations configurations;


    public LivyTaskSubmitHelper(Configurations configurations) {
        this.configurations = configurations;
        init();
    }

    /**
     * Initialize related parameters
     */
    public void init() {
        uri = configurations.getString("livy.uri");
        logger.info("Livy uri : {}", uri);
    }

    public Map<String, Object> retryLivyGetAppId(String result, int appIdRetryCount) {

        int retryCount = appIdRetryCount;
        TypeReference<HashMap<String, Object>> type =
                new TypeReference<HashMap<String, Object>>() {
                };
        Map<String, Object> resultMap = JSONUtils.parseObject(result, type);

        if (retryCount <= 0) {
            return null;
        }

        if (resultMap.get("appId") != null) {
            return resultMap;
        }

        Object livyBatchesId = resultMap.get("id");
        if (livyBatchesId == null) {
            return resultMap;
        }

        while (retryCount-- > 0) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            resultMap = getResultByLivyId(livyBatchesId, type);
            logger.info("retry get livy resultMap: {}, batches id : {}", resultMap, livyBatchesId);

            if (resultMap.get("appId") != null) {
                break;
            }
        }

        return resultMap;
    }

    public Map<String, Object> getResultByLivyId(Object livyBatchesId, TypeReference<HashMap<String, Object>> type) {
        Map<String, Object> resultMap = new HashedMap();
        String livyUri = uri + "/" + livyBatchesId;
        String result = getFromLivy(livyUri);
        logger.info(result);
        return result == null ? resultMap : JSONUtils.parseObject(result, type);
    }

    public String postToLivy(String livyArgs) {

        String needKerberos = configurations.getString("livy.need.kerberos");
        logger.info("Need Kerberos:" + needKerberos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(REQUEST_BY_HEADER, "admin");

        if (needKerberos == null || needKerberos.isEmpty()) {
            logger.error("The property \"livy.need.kerberos\" is empty");
            return null;
        }

        if (needKerberos.equalsIgnoreCase("false")) {
            logger.info("The livy server doesn't need Kerberos Authentication");
            String result = null;
            try {
                HttpEntity<String> springEntity = new HttpEntity<>(livyArgs, headers);
                result = restTemplate.postForObject(uri, springEntity, String.class);
                logger.info(result);
            } catch (HttpClientErrorException e) {
                logger.error("Post to livy ERROR. \n  response status : " + e.getMessage()
                        + "\n  response header : " + e.getResponseHeaders()
                        + "\n  response body : " + e.getResponseBodyAsString());
            } catch (Exception e) {
                logger.error("Post to livy ERROR. \n {}", e);
            }
            return result;
        } else {
            logger.info("The livy server needs Kerberos Authentication");
            String userPrincipal = configurations.getString("livy.server.auth.kerberos.principal");
            String keyTabLocation = configurations.getString("livy.server.auth.kerberos.keytab");
            logger.info("principal:{}, lcoation:{}", userPrincipal, keyTabLocation);

            KerberosRestTemplate restTemplate = new KerberosRestTemplate(keyTabLocation, userPrincipal);
            HttpEntity<String> springEntity = null;
            String result = null;
            try {
                springEntity = new HttpEntity<>(livyArgs, headers);
                result = restTemplate.postForObject(uri, springEntity, String.class);
                logger.info(result);
            } catch (HttpClientErrorException e) {
                logger.error("Post to livy ERROR. \n  response status : " + e.getMessage()
                        + "\n  response header : " + e.getResponseHeaders()
                        + "\n  response body : " + e.getResponseBodyAsString());
            } catch (Exception e) {
                logger.error("Post to livy ERROR. {}", e.getMessage(), e);
            }

            return result;
        }
    }

    public String getFromLivy(String uri) {

        logger.info("Get From Livy URI is: " + uri);
        String needKerberos = configurations.getString("livy.need.kerberos");
        logger.info("Need Kerberos:" + needKerberos);

        if (needKerberos == null || needKerberos.isEmpty()) {
            logger.error("The property \"livy.need.kerberos\" is empty");
            return null;
        }

        if (needKerberos.equalsIgnoreCase("false")) {
            logger.info("The livy server doesn't need Kerberos Authentication");
            return restTemplate.getForObject(uri, String.class);
        } else {
            logger.info("The livy server needs Kerberos Authentication");
            String userPrincipal = configurations.getString("livy.server.auth.kerberos.principal");
            String keyTabLocation = configurations.getString("livy.server.auth.kerberos.keytab");
            logger.info("principal:{}, lcoation:{}", userPrincipal, keyTabLocation);

            KerberosRestTemplate restTemplate = new KerberosRestTemplate(keyTabLocation, userPrincipal);
            String result = restTemplate.getForObject(uri, String.class);
            logger.info(result);
            return result;
        }
    }

    public void deleteByLivy(Long sessionId, String appId) {

        String url = uri + "/" + sessionId;

        try {
            deleteByLivy(url);
            logger.info("The Livy job ({}) has been deleted. {}", sessionId, url);
        } catch (ResourceAccessException e) {
            logger.error("Your url may be wrong. Please check {}. \n {}",
                    uri, e.getMessage());
        } catch (RestClientException e) {
            logger.warn("sessionId({}) {}.", url,
                    e.getMessage());
            deleteByYarn(configurations.getString("yarn.uri"), appId);
        }
    }

    public void deleteByLivy(String uri) {

        logger.info("Delete by Livy URI is: " + uri);
        String needKerberos = configurations.getString("livy.need.kerberos");
        logger.info("Need Kerberos:" + needKerberos);

        if (needKerberos == null || needKerberos.isEmpty()) {
            logger.error("The property \"livy.need.kerberos\" is empty");
            return;
        }

        if (needKerberos.equalsIgnoreCase("false")) {
            logger.info("The livy server doesn't need Kerberos Authentication");
            new RestTemplate().delete(uri);
        } else {
            logger.info("The livy server needs Kerberos Authentication");
            String userPrincipal = configurations.getString("livy.server.auth.kerberos.principal");
            String keyTabLocation = configurations.getString("livy.server.auth.kerberos.keytab");
            logger.info("principal:{}, lcoation:{}", userPrincipal, keyTabLocation);

            KerberosRestTemplate restTemplate = new KerberosRestTemplate(keyTabLocation, userPrincipal);
            restTemplate.delete(uri);
        }
    }

    /**
     * delete app task scheduling by yarn.
     *
     * @param url   prefix part of whole url
     * @param appId application id
     */
    public void deleteByYarn(String url, String appId) {
        try {
            if (url != null && appId != null) {
                logger.info("{} will delete by yarn", appId);
                restTemplate.put(url + "ws/v1/cluster/apps/"
                                + appId + "/state",
                        "{\"state\": \"KILLED\"}");
            }
        } catch (HttpClientErrorException e) {
            logger.warn("client error {} from yarn: {}",
                    e.getMessage(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("delete exception happens by yarn. {}", e);
        }
    }
}
