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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 插件隔离 ClassLoader，每个 (pluginName + version) 独享一个实例。
 *
 * <p>类加载委托策略：
 * <ol>
 *   <li>先检查已加载缓存</li>
 *   <li>SPI 白名单包 → 强制从 {@code spiClassLoader} 加载，确保接口 Class 对象唯一</li>
 *   <li>其他 → 优先从插件自身 URL 集合加载；本地不存在时再回退到宿主 ClassLoader</li>
 * </ol>
 *
 * <p>SPI 包白名单保证：无论插件 JAR 中是否打包了相同类，关键接口与共享注解始终来自同一个
 * ClassLoader，避免 {@code ClassCastException}、Jackson 注解失效等跨 ClassLoader
 * 类型不兼容问题。非白名单类走 plugin-first，可让插件私有依赖按版本隔离。
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
        // 父仍为 spiClassLoader，使插件在本地缺失非 SPI 类时可以回退到宿主 libs/ 中的共享依赖。
        // 真正的查找顺序由 loadClass/getResource/getResources 控制，而不是 URLClassLoader 默认的
        // parent-first 语义：SPI 白名单始终走父加载器，其他类/资源先查插件自身，再回退宿主。
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

            // 3. 其他类：plugin-first；本地不存在再回退到宿主
            try {
                return resolveIfNeeded(findClass(name), resolve);
            } catch (ClassNotFoundException ignored) {
                return resolveIfNeeded(super.loadClass(name, false), resolve);
            }
        }
    }

    @Override
    public URL getResource(String name) {
        if (isSpiResource(name)) {
            return spiClassLoader.getResource(name);
        }
        URL local = findResource(name);
        if (local != null) {
            return local;
        }
        return spiClassLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (isSpiResource(name)) {
            return spiClassLoader.getResources(name);
        }

        Set<URL> resources = new LinkedHashSet<URL>();
        appendResources(resources, findResources(name));
        appendResources(resources, spiClassLoader.getResources(name));
        return Collections.enumeration(resources);
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

    private void appendResources(Set<URL> target, Enumeration<URL> resources) {
        while (resources.hasMoreElements()) {
            target.add(resources.nextElement());
        }
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
