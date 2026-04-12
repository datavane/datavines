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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * 从当前 classpath 扫描并加载多版本插件（用于 IDE/开发模式）。
 *
 * <p>在 IDE（如 IntelliJ IDEA）中运行时，所有插件模块都在同一个 ClassLoader 上，
 * 不支持真正的 ClassLoader 隔离。但仍然可以通过读取各插件的
 * {@code META-INF/datavines-plugin.properties} 来构建 {@link VersionedPluginRegistry}，
 * 获得版本感知能力。
 *
 * <p>加载流程：
 * <ol>
 *   <li>使用 ServiceLoader 发现所有插件实现</li>
 *   <li>扫描 classpath 上所有 datavines-plugin.properties 文件，构建插件名→描述符索引</li>
 *   <li>对每个实现类，通过 keyExtractor 提取插件名，匹配描述符</li>
 *   <li>注册到 VersionedPluginRegistry</li>
 *   <li>无描述符的插件使用默认版本 "0.0.0"</li>
 * </ol>
 *
 * <p>使用示例：
 * <pre>{@code
 * ClasspathPluginLoader loader = new ClasspathPluginLoader();
 *
 * // 单 key 插件
 * VersionedPluginRegistry<ConnectorFactory> registry =
 *     loader.load(ConnectorFactory.class, ConnectorFactory::getPluginName);
 *
 * // 多 key 插件
 * VersionedPluginRegistry<ExpectedValue> registry =
 *     loader.loadMultiKey(ExpectedValue.class, ExpectedValue::getPluginNames);
 * }</pre>
 */
public final class ClasspathPluginLoader {

    private static final Logger log = LoggerFactory.getLogger(ClasspathPluginLoader.class);

    private static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";

    private final ClassLoader classLoader;

    public ClasspathPluginLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClasspathPluginLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 从 classpath 加载单 key 插件。
     *
     * @param serviceType  SPI 接口
     * @param keyExtractor 从插件实例提取名称的函数
     * @return VersionedPluginRegistry
     */
    public <P> VersionedPluginRegistry<P> load(Class<P> serviceType, Function<P, String> keyExtractor) {
        DescriptorIndex descriptorIndex = scanDescriptors();
        List<P> providers = ServiceLoaderUtils.loadAll(serviceType, classLoader);

        VersionedPluginRegistry.Builder<P> builder =
                VersionedPluginRegistry.builder(serviceType.getSimpleName());

        for (P provider : providers) {
            String pluginName = keyExtractor.apply(provider);
            if (pluginName == null) {
                log.warn("Provider {} returned null plugin name, skipping",
                        provider.getClass().getName());
                continue;
            }

            PluginDescriptor descriptor = descriptorIndex.findDescriptor(pluginName, provider);
            registerSafely(builder, descriptor, provider);
        }

        VersionedPluginRegistry<P> registry = builder.build();
        log.info("ClasspathPluginLoader loaded {} plugins with {} total versions for {}",
                registry.supportedPluginNames().size(),
                countTotalVersions(registry),
                serviceType.getSimpleName());
        return registry;
    }

    /**
     * 从 classpath 加载多 key 插件（同一个实例注册多个名称）。
     *
     * @param serviceType   SPI 接口
     * @param keysExtractor 从插件实例提取多个名称的函数
     * @return VersionedPluginRegistry
     */
    public <P> VersionedPluginRegistry<P> loadMultiKey(Class<P> serviceType,
                                                        Function<P, Collection<String>> keysExtractor) {
        DescriptorIndex descriptorIndex = scanDescriptors();
        List<P> providers = ServiceLoaderUtils.loadAll(serviceType, classLoader);

        VersionedPluginRegistry.Builder<P> builder =
                VersionedPluginRegistry.builder(serviceType.getSimpleName());

        for (P provider : providers) {
            Collection<String> pluginNames = keysExtractor.apply(provider);
            if (pluginNames == null || pluginNames.isEmpty()) {
                log.warn("Provider {} returned empty plugin names, skipping",
                        provider.getClass().getName());
                continue;
            }

            for (String pluginName : pluginNames) {
                if (pluginName == null || pluginName.trim().isEmpty()) {
                    log.warn("Provider {} returned blank plugin name, skipping",
                            provider.getClass().getName());
                    continue;
                }

                String normalizedName = pluginName.trim();
                PluginDescriptor descriptor = descriptorIndex.findDescriptor(normalizedName, provider);
                registerSafely(builder, normalizedName, descriptor, provider);
            }
        }

        VersionedPluginRegistry<P> registry = builder.build();
        log.info("ClasspathPluginLoader loaded {} plugins for {}",
                registry.supportedPluginNames().size(), serviceType.getSimpleName());
        return registry;
    }

    /**
     * 描述符索引，支持双重匹配策略：
     * <ol>
     *   <li>按 plugin.name 精确匹配（适合单 key 插件）</li>
     *   <li>按类来源 URL 匹配（适合多 key 插件，同一模块注册多个名称）</li>
     * </ol>
     *
     * <p>在 IDE 模式下，provider 类来自 target/classes/ 目录，描述符 URL 类似：
     * {@code file:/path/to/module/target/classes/META-INF/datavines-plugin.properties}。
     * provider 的 CodeSource URL 类似：{@code file:/path/to/module/target/classes/}。
     * 当 descriptor URL 以 CodeSource URL 为前缀时，即表示来自同一模块。
     */
    static final class DescriptorIndex {
        private final Map<String, PluginDescriptor> byName;
        private final Map<String, PluginDescriptor> byUrlPrefix;

        DescriptorIndex(Map<String, PluginDescriptor> byName,
                        Map<String, PluginDescriptor> byUrlPrefix) {
            this.byName = byName;
            this.byUrlPrefix = byUrlPrefix;
        }

        /**
         * 查找匹配的描述符。优先按名称匹配，其次按类来源 URL 匹配。
         * 都找不到则创建默认描述符。
         */
        <P> PluginDescriptor findDescriptor(String pluginName, P provider) {
            // 1. 按 plugin.name 精确匹配
            PluginDescriptor descriptor = byName.get(pluginName);
            if (descriptor != null) {
                return descriptor;
            }

            // 2. 按类来源 URL 匹配（同一模块的不同 plugin name）
            descriptor = findByClassOrigin(provider);
            if (descriptor != null) {
                log.debug("Matched plugin '{}' to descriptor {} by class origin",
                        pluginName, descriptor.getPluginId());
                return descriptor;
            }

            // 3. 回退：使用默认版本
            log.debug("No descriptor found for plugin '{}' (class: {}), using default version {}",
                    pluginName, provider.getClass().getName(), DEFAULT_VERSION);
            return PluginDescriptor.of(pluginName, DEFAULT_VERSION);
        }

        private <P> PluginDescriptor findByClassOrigin(P provider) {
            String classOrigin = getCodeSourceUrl(provider.getClass());
            if (classOrigin == null) {
                return null;
            }
            for (Map.Entry<String, PluginDescriptor> entry : byUrlPrefix.entrySet()) {
                if (entry.getKey().startsWith(classOrigin)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        private static String getCodeSourceUrl(Class<?> clazz) {
            try {
                ProtectionDomain pd = clazz.getProtectionDomain();
                if (pd == null) {
                    return null;
                }
                CodeSource cs = pd.getCodeSource();
                if (cs == null || cs.getLocation() == null) {
                    return null;
                }
                return cs.getLocation().toString();
            } catch (SecurityException e) {
                return null;
            }
        }
    }

    /**
     * 扫描 classpath 上所有 datavines-plugin.properties，构建双索引。
     */
    private DescriptorIndex scanDescriptors() {
        Map<String, PluginDescriptor> byName = new LinkedHashMap<String, PluginDescriptor>();
        Map<String, PluginDescriptor> byUrlPrefix = new LinkedHashMap<String, PluginDescriptor>();
        try {
            Enumeration<URL> resources = classLoader.getResources(PluginDescriptor.DESCRIPTOR_PATH);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream is = url.openStream()) {
                    Properties props = new Properties();
                    props.load(is);

                    String name = props.getProperty("plugin.name");
                    String version = props.getProperty("plugin.version");

                    if (name == null || name.trim().isEmpty()) {
                        log.debug("Skipping descriptor without plugin.name: {}", url);
                        continue;
                    }
                    if (version == null || version.trim().isEmpty()) {
                        version = DEFAULT_VERSION;
                    }

                    String spiVersion = props.getProperty("plugin.spi.version", "0.0.0");
                    String mainRange = props.getProperty("plugin.main.version.range", "");
                    String description = props.getProperty("plugin.description", "");
                    String module = props.getProperty("plugin.module", "");

                    PluginDescriptor desc = PluginDescriptor.of(
                            name.trim(), module.trim(), version.trim(),
                            spiVersion.trim(),
                            mainRange.trim(), description.trim());

                    byName.put(name.trim(), desc);
                    byUrlPrefix.put(url.toString(), desc);
                    log.debug("Scanned descriptor: {} from {}", desc.getPluginId(), url);

                } catch (Exception e) {
                    log.warn("Failed to parse descriptor from {}: {}", url, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan for plugin descriptors: {}", e.getMessage());
        }
        log.debug("Scanned {} plugin descriptors from classpath", byName.size());
        return new DescriptorIndex(byName, byUrlPrefix);
    }

    /**
     * 安全注册，捕获重复版本异常（在 classpath 模式下可能因为同名插件导致）。
     */
    private <P> void registerSafely(VersionedPluginRegistry.Builder<P> builder,
                                     PluginDescriptor descriptor, P plugin) {
        try {
            builder.register(descriptor, plugin, null);
        } catch (DuplicateProviderException e) {
            // 在 classpath 模式下，同名插件只有一个版本，重复是预期的
            log.debug("Skipping duplicate registration for {}: {}",
                    descriptor.getPluginId(), e.getMessage());
        }
    }

    private <P> void registerSafely(VersionedPluginRegistry.Builder<P> builder,
                                    String pluginName,
                                    PluginDescriptor descriptor,
                                    P plugin) {
        try {
            builder.register(pluginName, descriptor, plugin, null);
        } catch (DuplicateProviderException e) {
            log.debug("Skipping duplicate registration for {} using logical key '{}': {}",
                    descriptor.getPluginId(), pluginName, e.getMessage());
        }
    }

    private <P> int countTotalVersions(VersionedPluginRegistry<P> registry) {
        int count = 0;
        for (String name : registry.supportedPluginNames()) {
            count += registry.getAllVersions(name).size();
        }
        return count;
    }
}
