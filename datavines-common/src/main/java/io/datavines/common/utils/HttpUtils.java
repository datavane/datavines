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
package io.datavines.common.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.common.CommonConstants;

/**
 * http utils
 */
public class HttpUtils {

    public static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private HttpUtils() {
        throw new UnsupportedOperationException("Construct HttpUtils");
    }

    public static CloseableHttpClient getInstance() {
        return HttpClientInstance.HTTP_CLIENT;
    }

    private static class HttpClientInstance {
        private static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom().setConnectionManager(CM).setDefaultRequestConfig(REQUEST_CONFIG).build();
    }


    private static final PoolingHttpClientConnectionManager CM;

    private static SSLContext ctx = null;

    private static final RequestConfig REQUEST_CONFIG;

    private static final X509TrustManager XTM = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    static {
        try {
            ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[]{XTM}, null);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SSLContext init with NoSuchAlgorithmException", e);
        } catch (KeyManagementException e) {
            logger.error("SSLContext init with KeyManagementException", e);
        }
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);

        // set timeout、request time、socket timeout
        REQUEST_CONFIG = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setExpectContinueEnabled(Boolean.TRUE)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                .setProxyPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC))
                .setConnectTimeout(CommonConstants.HTTP_CONNECT_TIMEOUT).setSocketTimeout(CommonConstants.SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(CommonConstants.HTTP_CONNECTION_REQUEST_TIMEOUT).setRedirectsEnabled(true)
                .build();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();
        CM = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        CM.setDefaultMaxPerRoute(60);
        CM.setMaxTotal(100);

    }

    /**
     * get http request content
     *
     * @param url url
     * @return http get request response content
     */
    public static String get(String url) {
        CloseableHttpClient httpclient = HttpUtils.getInstance();

        HttpGet httpget = new HttpGet(url);
        return getResponseContentString(httpget, httpclient);
    }

    public static String post(String url, String body, Map<String,String> headers) {
        CloseableHttpClient httpclient = HttpUtils.getInstance();

        StringEntity stringEntity=new StringEntity(body, ContentType.APPLICATION_JSON);
        HttpPost httpPost = new HttpPost(url);
        if(headers != null && !headers.isEmpty()){
            headers.forEach((k, v) -> httpPost.addHeader(k,v));
        }

        httpPost.setEntity(stringEntity);
        return getResponseContentString(httpPost, httpclient);
    }

    /**
     * get http response content
     *
     * @param httpget httpget
     * @param httpClient httpClient
     * @return http get request response content
     */
    public static String getResponseContentString(HttpRequestBase httpget, CloseableHttpClient httpClient) {
        String responseContent = null;
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpget);
            // check response status is 200
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    responseContent = EntityUtils.toString(entity, CommonConstants.UTF_8);
                } else {
                    logger.warn("http entity is null");
                }
            } else {
                logger.error("http get:{} response status code is not 200!", response.getStatusLine().getStatusCode());
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            if (!httpget.isAborted()) {
                httpget.releaseConnection();
                httpget.abort();
            }

        }
        return responseContent;
    }

}
