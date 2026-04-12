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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 从文件系统目录扫描并加载多版本插件。
 *
 * <p>目录规范。module/name/version 均以 JAR 内
 * {@code META-INF/datavines-plugin.properties} 的 {@code plugin.module}、
 * {@code plugin.name}、{@code plugin.version} 为准：
 * <pre>
 * {plugin.dir}/
 * ├── connector/
 * │   ├── mysql/
 * │   │   ├── 5.7.44/
 * │   │   │   ├── datavines-connector-mysql-5.7.44.jar
 * │   │   │   └── mysql-connector-j-5.1.49.jar
 * │   │   └── 8.0.33/
 * │   │       ├── datavines-connector-mysql-8.0.33.jar
 * │   │       └── mysql-connector-j-8.0.33.jar
 * │   └── postgresql/
 * │       └── 42.7.0/
 * │           └── datavines-connector-postgresql.jar
 * </pre>
 *
 * <p>扫描逻辑：标准结构为三层目录 {@code plugins/{module}/{name}/{version}/}，
 * 同时兼容旧的两层目录 {@code plugins/{name}/{version}/}。
 * 每个版本目录创建独立的 {@link PluginClassLoader}，实现类加载隔离。
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
        return loadInternal(serviceType, null);
    }

    /**
     * 加载支持多逻辑 key 的 SPI。
     *
     * <p>同一个 provider 可以通过 {@code keysExtractor} 暴露多个逻辑名称，
     * 这些逻辑名称将被分别注册到 {@link VersionedPluginRegistry}。
     */
    public <P> VersionedPluginRegistry<P> loadMultiKey(
            Class<P> serviceType, Function<P, Collection<String>> keysExtractor) {
        if (keysExtractor == null) {
            throw new IllegalArgumentException("keysExtractor cannot be null");
        }
        return loadInternal(serviceType, keysExtractor);
    }

    private <P> VersionedPluginRegistry<P> loadInternal(
            Class<P> serviceType, Function<P, Collection<String>> keysExtractor) {
        VersionedPluginRegistry.Builder<P> builder =
                VersionedPluginRegistry.builder(serviceType.getSimpleName());

        List<Path> versionDirs = scanVersionDirs();
        log.info("Discovered {} plugin version directories for {}", versionDirs.size(), serviceType.getSimpleName());

        for (Path versionDir : versionDirs) {
            loadVersionDir(versionDir, serviceType, keysExtractor, builder);
        }

        return builder.build();
    }

    private <P> void loadVersionDir(
            Path versionDir, Class<P> serviceType,
            Function<P, Collection<String>> keysExtractor,
            VersionedPluginRegistry.Builder<P> builder) {

        List<URL> urls = collectJars(versionDir);
        if (urls.isEmpty()) {
            log.debug("No JAR files found in {}, skipping", versionDir);
            return;
        }

        // 从目录名推断 bundle name 和 version。逻辑插件名以 descriptor 为准。
        Path nameDir = versionDir.getParent();
        if (nameDir == null) {
            log.warn("Skipping malformed plugin version directory without parent: {}", versionDir);
            return;
        }

        String inferredBundleName = nameDir.getFileName().toString();
        String inferredVersion = versionDir.getFileName().toString();
        String inferredModule = inferModule(versionDir);
        String pluginId = inferredBundleName + "@" + inferredVersion;

        PluginClassLoader classLoader = new PluginClassLoader(
                pluginId, urls, spiClassLoader, spiPackages);

        try {
            // 从 JAR 内读取 descriptor
            PluginDescriptor descriptor = PluginDescriptor.load(classLoader);
            if (descriptor == null) {
                log.warn("Missing {} in plugin jars under {}. "
                        + "Using inferred metadata: name={}, version={}",
                        PluginDescriptor.DESCRIPTOR_PATH, versionDir,
                        inferredBundleName, inferredVersion);
                // 容错：使用推断的元数据
                descriptor = PluginDescriptor.of(inferredBundleName, inferredModule, inferredVersion,
                        "0.0.0", "", "");
            }

            // 校验 descriptor 与目录结构一致性
            validateDescriptor(descriptor, inferredModule, inferredBundleName, inferredVersion);

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

                if (keysExtractor == null && providers.size() > 1) {
                    log.error("Found {} providers of {} in {}, expected at most 1. Skipping this directory.",
                            providers.size(), serviceType.getSimpleName(), versionDir);
                    return;
                }

                if (keysExtractor == null) {
                    P plugin = providers.get(0);
                    log.info("  Registering {} -> {}", descriptor.getPluginId(), plugin.getClass().getName());
                    builder.register(descriptor, plugin, classLoader);
                    return;
                }

                Map<String, P> keyedPlugins = new LinkedHashMap<String, P>();
                for (P provider : providers) {
                    Collection<String> keys = keysExtractor.apply(provider);
                    if (keys == null || keys.isEmpty()) {
                        log.warn("Provider {} returned empty keys in {}, skipping",
                                provider.getClass().getName(), versionDir);
                        continue;
                    }

                    for (String key : keys) {
                        if (key == null || key.trim().isEmpty()) {
                            log.warn("Provider {} returned blank key in {}, skipping",
                                    provider.getClass().getName(), versionDir);
                            continue;
                        }

                        String normalizedKey = key.trim();
                        P previous = keyedPlugins.put(normalizedKey, provider);
                        if (previous != null && previous != provider) {
                            throw new DuplicateProviderException(
                                    serviceType.getSimpleName(),
                                    normalizedKey + "@" + descriptor.getVersion(),
                                    previous.getClass().getName(),
                                    provider.getClass().getName());
                        }
                    }
                }

                if (keyedPlugins.isEmpty()) {
                    log.debug("No logical {} key found in {}, skipping",
                            serviceType.getSimpleName(), versionDir);
                    return;
                }

                log.info("  Registering {} logical key(s) from {}",
                        keyedPlugins.size(), descriptor.getPluginId());
                builder.registerAll(descriptor, keyedPlugins, classLoader);
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
     * 扫描所有 plugins/{module}/{name}/{version}/ 目录，同时兼容 plugins/{name}/{version}/。
     * 使用显式 try-with-resources 管理 DirectoryStream，避免资源泄漏。
     */
    private List<Path> scanVersionDirs() {
        List<Path> result = new ArrayList<Path>();

        for (Path root : pluginRootDirs) {
            if (!Files.isDirectory(root)) {
                log.warn("Plugin root directory does not exist: {}", root);
                continue;
            }

            // 标准结构：root/module/name/version；兼容旧结构：root/name/version。
            List<Path> firstLevelDirs = listDirectories(root);
            for (Path firstLevelDir : firstLevelDirs) {
                List<Path> secondLevelDirs = listDirectories(firstLevelDir);
                for (Path secondLevelDir : secondLevelDirs) {
                    List<Path> thirdLevelDirs = listDirectories(secondLevelDir);
                    if (thirdLevelDirs.isEmpty()) {
                        result.add(secondLevelDir);
                    } else {
                        result.addAll(thirdLevelDirs);
                    }
                }
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
            List<Path> jars = new ArrayList<Path>();
            for (Path jar : stream) {
                if (Files.isRegularFile(jar)) {
                    jars.add(jar);
                }
            }
            Collections.sort(jars, new Comparator<Path>() {
                @Override
                public int compare(Path left, Path right) {
                    return left.getFileName().toString().compareTo(right.getFileName().toString());
                }
            });

            for (Path jar : jars) {
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
     * 校验描述符与目录结构的一致性。
     */
    private void validateDescriptor(PluginDescriptor descriptor,
                                    String inferredModule,
                                    String inferredBundleName,
                                    String inferredVersion) {
        if (descriptor.getPluginModule() != null
                && !descriptor.getPluginModule().isEmpty()
                && !descriptor.getPluginModule().equals(inferredModule)) {
            log.warn("Plugin module mismatch: descriptor says '{}', directory says '{}'. "
                            + "Directory placement should be corrected.",
                    descriptor.getPluginModule(), inferredModule);
        }

        if (descriptor.getPluginName() != null
                && !descriptor.getPluginName().isEmpty()
                && !descriptor.getPluginName().equals(inferredBundleName)) {
            log.warn("Plugin name mismatch: descriptor says '{}', directory says '{}'. "
                            + "Directory placement should be corrected.",
                    descriptor.getPluginName(), inferredBundleName);
        }

        log.debug("Plugin descriptor '{}' loaded from bundle directory '{}'.",
                descriptor.getPluginName(), inferredBundleName);

        String descriptorVersion = descriptor.getVersion().toString();
        if (!descriptorVersion.equals(inferredVersion)) {
            log.warn("Plugin version mismatch: descriptor says '{}', directory says '{}'. "
                    + "Directory placement should be corrected.", descriptorVersion, inferredVersion);
        }
    }

    private String inferModule(Path versionDir) {
        for (Path root : pluginRootDirs) {
            if (!versionDir.startsWith(root)) {
                continue;
            }

            Path relative = root.relativize(versionDir);
            if (relative.getNameCount() == 2) {
                return "";
            }
            if (relative.getNameCount() >= 3) {
                return relative.getName(0).toString();
            }
        }
        return "";
    }

    private static void closeQuietly(PluginClassLoader classLoader) {
        try {
            classLoader.close();
        } catch (Exception ignored) {
            // ignored
        }
    }
}
