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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.Md5Utils;
import io.datavines.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HiveDriverClassLoader {

    private static final ConcurrentHashMap<String, URLClassLoader> classLoaderMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Driver> driverMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, HikariDataSource> dataSourceMap = new ConcurrentHashMap<>();

    private static final String HIVE_DRIVER_CLASS = "org.apache.hive.jdbc.HiveDriver";
    private static final Set<String> SUPPORTED_VERSIONS = new HashSet<>(Arrays.asList("2.1", "3.1"));

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(HiveDriverClassLoader::closeAll, "HiveDriverClassLoader-shutdown"));
    }

    public static URLClassLoader getClassLoader(String hiveVersion) {
        if (!SUPPORTED_VERSIONS.contains(hiveVersion)) {
            throw new DataVinesException("Unsupported Hive version: " + hiveVersion
                    + ". Supported versions: " + SUPPORTED_VERSIONS);
        }

        return classLoaderMap.computeIfAbsent(hiveVersion, v -> {
            File driverDir = getDriverDirectory(v);
            if (!driverDir.exists() || !driverDir.isDirectory()) {
                throw new DataVinesException("Hive " + v + " driver directory not found: " + driverDir.getAbsolutePath()
                        + ". Please place the hive-jdbc JARs in this directory.");
            }

            File[] jars = driverDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jars == null || jars.length == 0) {
                throw new DataVinesException("No JAR files found in Hive " + v + " driver directory: " + driverDir.getAbsolutePath());
            }

            URL[] urls = Arrays.stream(jars).map(f -> {
                try {
                    return f.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new DataVinesException("Failed to convert JAR path to URL: " + f.getAbsolutePath(), e);
                }
            }).toArray(URL[]::new);

            log.info("Creating isolated ClassLoader for Hive {} with {} JARs from {}", v, urls.length, driverDir.getAbsolutePath());
            return new URLClassLoader(urls, ClassLoader.getSystemClassLoader().getParent());
        });
    }

    public static Driver getDriver(String hiveVersion) {
        return driverMap.computeIfAbsent(hiveVersion, v -> {
            try {
                ClassLoader cl = getClassLoader(v);
                Class<?> driverClass = Class.forName(HIVE_DRIVER_CLASS, true, cl);
                Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
                log.info("Loaded Hive {} JDBC driver successfully", v);
                return driver;
            } catch (DataVinesException e) {
                throw e;
            } catch (Exception e) {
                throw new DataVinesException("Failed to load Hive " + v + " JDBC driver", e);
            }
        });
    }

    public static DataSource getDataSource(String hiveVersion, String jdbcUrl, String user, String password) {
        String key = Md5Utils.getMd5(hiveVersion + "@@" + jdbcUrl + "@@" + user + "@@" + password, false);
        return dataSourceMap.computeIfAbsent(key, k -> {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(user);
            config.setPassword(StringUtils.isEmptyOrNullStr(password) ? null : password);
            config.setMaximumPoolSize(10);

            ClassLoader saved = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getClassLoader(hiveVersion));
                config.setDriverClassName(HIVE_DRIVER_CLASS);
                log.info("Creating HikariDataSource for Hive {} , url: {}", hiveVersion, jdbcUrl);
                return new HikariDataSource(config);
            } finally {
                Thread.currentThread().setContextClassLoader(saved);
            }
        });
    }

    private static File getDriverDirectory(String hiveVersion) {
        if (hiveVersion == null || hiveVersion.isEmpty()) {
            throw new DataVinesException("hiveVersion must not be null or empty");
        }
        String home = System.getenv("DATAVINES_HOME");
        if (home == null || home.isEmpty()) {
            home = System.getProperty("user.dir");
        }
        String dirName = "hive" + hiveVersion.substring(0, 1);
        return new File(home, "drivers" + File.separator + dirName);
    }

    public static void closeAll() {
        dataSourceMap.values().forEach(ds -> {
            if (!ds.isClosed()) {
                try {
                    ds.close();
                } catch (Exception e) {
                    log.warn("Failed to close HikariDataSource", e);
                }
            }
        });
        dataSourceMap.clear();
        driverMap.clear();
        classLoaderMap.values().forEach(cl -> {
            try {
                cl.close();
            } catch (Exception e) {
                log.warn("Failed to close ClassLoader", e);
            }
        });
        classLoaderMap.clear();
    }
}
