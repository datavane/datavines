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
package io.datavines.connector.plugin;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.datavines.common.datasource.jdbc.BaseJdbcDataSourceInfo;
import io.datavines.common.datasource.jdbc.JdbcConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MongodbClientManager {

    private final Logger logger = LoggerFactory.getLogger(MongodbClientManager.class);

    private final LoadingCache<JdbcConnectionInfo, MongoClient> mongoClientLoadingCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener((RemovalListener<JdbcConnectionInfo, MongoClient>) notification -> {
                notification.getValue().close();
            })
            .build(new CacheLoader<JdbcConnectionInfo, MongoClient>() {

                @Override
                public MongoClient load(JdbcConnectionInfo jdbcConnectionInfo) {
                    try {
                        BaseJdbcDataSourceInfo dataSourceInfo = new MongodbDataSourceInfo(jdbcConnectionInfo);
                        MongoClientSettings.Builder options = MongoClientSettings.builder();
                        String url = dataSourceInfo.getJdbcUrl();
                        ConnectionString connectionString = new ConnectionString(url);
                        options.applyConnectionString(connectionString);
                        return MongoClients.create(options.build());
                    } catch (Exception exception) {
                        logger.error(exception.toString(), exception);
                    }

                    return null;
                }
            });

    private static final class Singleton {
        private static final MongodbClientManager INSTANCE = new MongodbClientManager();
    }

    public static MongodbClientManager getInstance() {
        return MongodbClientManager.Singleton.INSTANCE;
    }

    public MongoClient getMongoClient(JdbcConnectionInfo jdbcConnectionInfo) {

        MongoClient mongoClient = null;
        try {
            mongoClient = mongoClientLoadingCache.get(jdbcConnectionInfo);
        } catch (ExecutionException e) {
            logger.error(e.toString(), e);
        }
        return mongoClient;
    }

}
