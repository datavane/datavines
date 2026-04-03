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
package io.datavines.spi.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 插件隔离 ClassLoader，每个 (pluginName + version) 独享一个实例。
 *
 * <p>类加载委托策略：
 * <ol>
 *   <li>先检查已加载缓存</li>
 *   <li>SPI 白名单包 → 强制从 {@code spiClassLoader} 加载，确保接口 Class 对象唯一</li>
 *   <li>其他 → 从插件自身 URL 集合加载（父为 Extension ClassLoader，避免宿主实现类泄漏）</li>
 * </ol>
 *
 * <p>SPI 包白名单保证：无论插件 JAR 中是否打包了相同类，关键接口始终来自同一个 ClassLoader，
 * 避免 {@code ClassCastException} 等跨 ClassLoader 类型不兼容问题。
 */
public final class PluginClassLoader extends URLClassLoader {

    /**
     * 必须从宿主加载的包前缀（接口 Class 必须来自同一个 ClassLoader，否则 instanceof 失效）。
     */
    public static final List<String> DEFAULT_SPI_PACKAGES = Collections.unmodifiableList(Arrays.asList(
            "io.datavines.spi.",
            "io.datavines.common.",
            "io.datavines.connector.api.",
            "io.datavines.metric.api.",
            "io.datavines.engine.api.",
            "io.datavines.engine.config.",
            "io.datavines.notification.api.",
            "io.datavines.registry.api.",
            // 共享第三方库（避免序列化/日志问题）
            "com.fasterxml.jackson.annotation.",
            "org.slf4j.",
            "io.opentelemetry.api.",
            "io.opentelemetry.context."
    ));

    private final String pluginId;
    private final ClassLoader spiClassLoader;
    private final List<String> spiPackages;

    /**
     * 使用默认 SPI 包列表构造。
     *
     * @param pluginId        插件唯一标识，如 "mysql@8.0.33"
     * @param urls            插件 JAR 文件 URL 列表
     * @param spiClassLoader  SPI 接口所在的 ClassLoader（通常为宿主 AppClassLoader）
     */
    public PluginClassLoader(String pluginId, List<URL> urls, ClassLoader spiClassLoader) {
        this(pluginId, urls, spiClassLoader, DEFAULT_SPI_PACKAGES);
    }

    /**
     * 使用自定义 SPI 包列表构造。
     *
     * @param pluginId        插件唯一标识
     * @param urls            插件 JAR 文件 URL 列表
     * @param spiClassLoader  SPI 接口所在的 ClassLoader
     * @param spiPackages     SPI 白名单包前缀列表
     */
    public PluginClassLoader(String pluginId, List<URL> urls,
                             ClassLoader spiClassLoader, List<String> spiPackages) {
        // 父为 spiClassLoader（即宿主 AppClassLoader），使插件可访问 libs/ 中的服务器类和共享依赖。
        // SPI 及共享 API 包通过白名单机制强制委托给 spiClassLoader，保证跨 ClassLoader 的类型唯一。
        // 如需完全隔离，可将父改为 ClassLoader.getSystemClassLoader().getParent()，
        // 但届时每个插件目录必须自包含所有非 SPI 依赖（包括 datavines-connector-jdbc 等）。
        super(urls.toArray(new URL[0]), spiClassLoader);
        this.pluginId = pluginId;
        this.spiClassLoader = spiClassLoader;
        this.spiPackages = Collections.unmodifiableList(new ArrayList<String>(spiPackages));
    }

    public String getPluginId() {
        return pluginId;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 1. 缓存命中
            Class<?> cached = findLoadedClass(name);
            if (cached != null) {
                return resolveIfNeeded(cached, resolve);
            }

            // 2. SPI 白名单 → 必须从 spiClassLoader 加载
            if (isSpiClass(name)) {
                try {
                    return resolveIfNeeded(spiClassLoader.loadClass(name), resolve);
                } catch (ClassNotFoundException e) {
                    // 安全检查：插件 JAR 不应包含 SPI 包中的类
                    if (existsLocally(name)) {
                        throw new ClassNotFoundException(
                                "SPI class '" + name + "' found in plugin '" + pluginId
                                + "' but not in SPI classloader. "
                                + "Please set the SPI dependency to <scope>provided</scope> in pom.xml.", e);
                    }
                    throw e;
                }
            }

            // 3. 插件自身的类
            return super.loadClass(name, resolve);
        }
    }

    @Override
    public URL getResource(String name) {
        if (isSpiResource(name)) {
            return spiClassLoader.getResource(name);
        }
        return super.getResource(name);
    }

    /**
     * 检查类名是否属于 SPI 白名单包。
     * 使用简单 for 循环而非 Stream，避免每次 loadClass 时创建 Stream 对象。
     */
    private boolean isSpiClass(String name) {
        for (int i = 0; i < spiPackages.size(); i++) {
            if (name.startsWith(spiPackages.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查资源路径是否属于 SPI 白名单包。
     */
    private boolean isSpiResource(String name) {
        for (int i = 0; i < spiPackages.size(); i++) {
            String pkgPath = spiPackages.get(i).replace('.', '/');
            if (name.startsWith(pkgPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查类是否存在于插件自身的 JAR 中（不触发类加载，仅检查资源）。
     * 优化：使用 findResource 而非 super.loadClass，避免加载副作用。
     */
    private boolean existsLocally(String name) {
        String path = name.replace('.', '/') + ".class";
        return findResource(path) != null;
    }

    private Class<?> resolveIfNeeded(Class<?> clazz, boolean resolve) {
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    public String toString() {
        return "PluginClassLoader[" + pluginId + "]";
    }
}
