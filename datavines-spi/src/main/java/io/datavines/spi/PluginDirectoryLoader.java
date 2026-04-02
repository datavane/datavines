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
package io.datavines.spi;

import io.datavines.spi.classloader.PluginClassLoader;
import io.datavines.spi.classloader.ThreadContextClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 从文件系统目录扫描并加载多版本插件。
 *
 * <p>目录规范：
 * <pre>
 * {plugin.dir}/
 * ├── mysql/
 * │   ├── 5.7.44/
 * │   │   ├── datavines-connector-mysql-5.7.44.jar
 * │   │   └── mysql-connector-j-5.1.49.jar
 * │   └── 8.0.33/
 * │       ├── datavines-connector-mysql-8.0.33.jar
 * │       └── mysql-connector-j-8.0.33.jar
 * └── postgresql/
 *     └── 42.7.0/
 *         └── datavines-connector-postgresql.jar
 * </pre>
 *
 * <p>扫描逻辑：两层目录 {@code plugins/{name}/{version}/} → 解析为 (pluginName, version)。
 * 每个 (name, version) 目录创建独立的 {@link PluginClassLoader}。
 *
 * <p>参考 Trino {@code ServerPluginsProvider}。
 */
public final class PluginDirectoryLoader {

    private static final Logger log = LoggerFactory.getLogger(PluginDirectoryLoader.class);

    private final List<Path> pluginRootDirs;
    private final ClassLoader spiClassLoader;
    private final List<String> spiPackages;
    private final PluginVersion currentHostVersion;

    /**
     * 使用默认配置构造。
     *
     * @param pluginRootDirs 插件根目录列表
     * @param spiClassLoader SPI 接口所在的 ClassLoader
     */
    public PluginDirectoryLoader(List<Path> pluginRootDirs, ClassLoader spiClassLoader) {
        this(pluginRootDirs, spiClassLoader,
                PluginClassLoader.DEFAULT_SPI_PACKAGES, null);
    }

    /**
     * 完整参数构造。
     *
     * @param pluginRootDirs     插件根目录列表
     * @param spiClassLoader     SPI 接口所在的 ClassLoader
     * @param spiPackages        SPI 白名单包前缀列表
     * @param currentHostVersion 当前宿主版本（用于兼容性校验，null 表示跳过校验）
     */
    public PluginDirectoryLoader(List<Path> pluginRootDirs, ClassLoader spiClassLoader,
                                 List<String> spiPackages, PluginVersion currentHostVersion) {
        this.pluginRootDirs = Collections.unmodifiableList(new ArrayList<Path>(pluginRootDirs));
        this.spiClassLoader = spiClassLoader;
        this.spiPackages = Collections.unmodifiableList(new ArrayList<String>(spiPackages));
        this.currentHostVersion = currentHostVersion;
    }

    /**
     * 加载某个 SPI 接口的所有版本插件，返回 {@link VersionedPluginRegistry}。
     *
     * @param serviceType SPI 接口（如 ConnectorFactory.class）
     * @return 包含所有已发现版本的注册表
     */
    public <P> VersionedPluginRegistry<P> load(Class<P> serviceType) {
        VersionedPluginRegistry.Builder<P> builder =
                VersionedPluginRegistry.builder(serviceType.getSimpleName());

        List<Path> versionDirs = scanVersionDirs();
        log.info("Discovered {} plugin version directories for {}", versionDirs.size(), serviceType.getSimpleName());

        for (Path versionDir : versionDirs) {
            loadVersionDir(versionDir, serviceType, builder);
        }

        return builder.build();
    }

    private <P> void loadVersionDir(
            Path versionDir, Class<P> serviceType,
            VersionedPluginRegistry.Builder<P> builder) {

        List<URL> urls = collectJars(versionDir);
        if (urls.isEmpty()) {
            log.debug("No JAR files found in {}, skipping", versionDir);
            return;
        }

        // 从目录名推断 pluginName 和 version
        String inferredName = versionDir.getParent().getFileName().toString();
        String inferredVersion = versionDir.getFileName().toString();
        String pluginId = inferredName + "@" + inferredVersion;

        PluginClassLoader classLoader = new PluginClassLoader(
                pluginId, urls, spiClassLoader, spiPackages);

        try {
            // 从 JAR 内读取 descriptor
            PluginDescriptor descriptor = PluginDescriptor.load(classLoader);
            if (descriptor == null) {
                log.warn("Missing {} in plugin jars under {}. "
                        + "Using inferred metadata: name={}, version={}",
                        PluginDescriptor.DESCRIPTOR_PATH, versionDir,
                        inferredName, inferredVersion);
                // 容错：使用推断的元数据
                descriptor = PluginDescriptor.of(inferredName, inferredVersion);
            }

            // 校验 descriptor 与目录名一致性
            validateDescriptor(descriptor, inferredName, inferredVersion);

            // 宿主版本兼容性校验
            if (currentHostVersion != null && !descriptor.isCompatibleWith(currentHostVersion)) {
                log.warn("Plugin {} is not compatible with host version {}. "
                        + "Declared range: {}. Skipping.",
                        descriptor.getPluginId(), currentHostVersion,
                        descriptor.getMainVersionRange());
                closeQuietly(classLoader);
                return;
            }

            log.info("Loading plugin {} from {}", descriptor.getPluginId(), versionDir);

            ThreadContextClassLoader ctxSwitch = new ThreadContextClassLoader(classLoader);
            try {
                List<P> providers = ServiceLoaderUtils.loadAll(serviceType, classLoader);

                if (providers.isEmpty()) {
                    // 此目录不包含该 SPI 类型的实现，正常跳过
                    log.debug("No {} provider found in {}, skipping", serviceType.getSimpleName(), versionDir);
                    return;
                }

                if (providers.size() > 1) {
                    log.error("Found {} providers of {} in {}, expected at most 1. Skipping this directory.",
                            providers.size(), serviceType.getSimpleName(), versionDir);
                    return;
                }

                P plugin = providers.get(0);
                log.info("  Registering {} -> {}", descriptor.getPluginId(), plugin.getClass().getName());
                builder.register(descriptor, plugin, classLoader);
            } finally {
                ctxSwitch.close();
            }

        } catch (DuplicateProviderException e) {
            log.error("Duplicate plugin detected in {}: {}", versionDir, e.getMessage());
            closeQuietly(classLoader);
        } catch (Exception e) {
            log.error("Failed to load plugin from {}: {}", versionDir, e.getMessage(), e);
            closeQuietly(classLoader);
        }
    }

    /**
     * 扫描所有 plugins/{name}/{version}/ 目录。
     * 使用显式 try-with-resources 管理 DirectoryStream，避免资源泄漏。
     */
    private List<Path> scanVersionDirs() {
        List<Path> result = new ArrayList<Path>();

        for (Path root : pluginRootDirs) {
            if (!Files.isDirectory(root)) {
                log.warn("Plugin root directory does not exist: {}", root);
                continue;
            }

            // 第一层：插件名目录
            List<Path> nameDirs = listDirectories(root);
            for (Path nameDir : nameDirs) {
                // 第二层：版本目录
                List<Path> versionDirs = listDirectories(nameDir);
                result.addAll(versionDirs);
            }
        }

        Collections.sort(result);
        return result;
    }

    /**
     * 列出指定目录下的所有子目录（不递归）。
     * 使用 DirectoryStream + try-with-resources 确保资源正确关闭。
     */
    private List<Path> listDirectories(Path parent) {
        List<Path> dirs = new ArrayList<Path>();
        DirectoryStream<Path> stream = null;
        try {
            stream = Files.newDirectoryStream(parent);
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    dirs.add(entry);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to list directory {}: {}", parent, e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
        return dirs;
    }

    /**
     * 收集目录下所有 .jar 文件的 URL。
     */
    private List<URL> collectJars(Path dir) {
        List<URL> urls = new ArrayList<URL>();
        DirectoryStream<Path> stream = null;
        try {
            stream = Files.newDirectoryStream(dir, "*.jar");
            for (Path jar : stream) {
                try {
                    urls.add(jar.toUri().toURL());
                } catch (MalformedURLException e) {
                    log.warn("Invalid JAR path: {}", jar, e);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to list JARs in {}: {}", dir, e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
        return urls;
    }

    /**
     * 校验描述符与目录名的一致性。
     */
    private void validateDescriptor(PluginDescriptor descriptor,
                                    String inferredName, String inferredVersion) {
        if (!descriptor.getPluginName().equals(inferredName)) {
            log.warn("Plugin name mismatch: descriptor says '{}', directory says '{}'. "
                    + "Using descriptor name.", descriptor.getPluginName(), inferredName);
        }

        String descriptorVersion = descriptor.getVersion().toString();
        if (!descriptorVersion.equals(inferredVersion)) {
            // 允许不完全匹配（如目录 "8.0" vs descriptor "8.0.0"），记录警告
            log.warn("Plugin version mismatch: descriptor says '{}', directory says '{}'. "
                    + "Using descriptor version.", descriptorVersion, inferredVersion);
        }
    }

    private static void closeQuietly(PluginClassLoader classLoader) {
        try {
            classLoader.close();
        } catch (Exception ignored) {
            // ignored
        }
    }
}
