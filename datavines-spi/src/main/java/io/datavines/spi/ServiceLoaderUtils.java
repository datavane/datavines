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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import io.datavines.spi.PluginDescriptor;

/**
 * ServiceLoader 的安全封装。
 *
 * <p>JDK ServiceLoader 在遇到单个 Provider 加载失败时，
 * 默认行为是抛出 ServiceConfigurationError 并中断整个迭代。
 * 本工具类提供容错模式：跳过失败的 Provider，继续加载其余的。
 */
public final class ServiceLoaderUtils {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoaderUtils.class);

    private ServiceLoaderUtils() {}

    /**
     * 安全加载所有 Provider。单个 Provider 加载失败时记录错误并跳过，不影响其他 Provider。
     *
     * @param serviceType SPI 服务接口
     * @return 成功加载的 Provider 列表
     */
    public static <S> List<S> loadAll(Class<S> serviceType) {
        return loadAll(serviceType, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 使用指定 ClassLoader 安全加载所有 Provider。
     */
    public static <S> List<S> loadAll(Class<S> serviceType, ClassLoader classLoader) {
        List<S> result = new ArrayList<>();
        ServiceLoader<S> loader = ServiceLoader.load(serviceType, classLoader);
        Iterator<S> iterator = loader.iterator();

        while (iterator.hasNext()) {
            try {
                result.add(iterator.next());
            } catch (ServiceConfigurationError e) {
                log.error("Failed to load provider for {}: {}",
                    serviceType.getName(), e.getMessage(), e);
            }
        }

        log.debug("Loaded {} providers for {}", result.size(), serviceType.getName());
        return result;
    }

    /**
     * 严格加载所有 Provider。任何 Provider 加载失败都会抛出异常。
     *
     * @param serviceType SPI 服务接口
     * @return 成功加载的 Provider 列表
     * @throws ServiceConfigurationError 如果任何 Provider 加载失败
     */
    public static <S> List<S> loadAllStrict(Class<S> serviceType) {
        List<S> result = new ArrayList<>();
        for (S provider : ServiceLoader.load(serviceType)) {
            result.add(provider);
        }
        return result;
    }

    /**
     * 使用指定 ClassLoader 加载，同时读取 {@link PluginDescriptor} 信息。
     * 返回 (descriptor, instance) 对列表，供 {@link VersionedPluginRegistry} 使用。
     *
     * @param serviceType SPI 服务接口
     * @param classLoader 指定的 ClassLoader
     * @return PluginEntry 列表
     */
    public static <S> List<PluginEntry<S>> loadWithDescriptor(
            Class<S> serviceType, ClassLoader classLoader) {

        List<PluginEntry<S>> result = new ArrayList<>();
        PluginDescriptor descriptor = PluginDescriptor.load(classLoader);

        ServiceLoader<S> loader = ServiceLoader.load(serviceType, classLoader);
        Iterator<S> it = loader.iterator();

        while (it.hasNext()) {
            try {
                S instance = it.next();
                result.add(new PluginEntry<S>(descriptor, instance));
            } catch (ServiceConfigurationError e) {
                log.error("Failed to load {} provider: {}",
                        serviceType.getName(), e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 包含插件描述符与实例的条目。
     *
     * @param <S> 插件实例类型
     */
    public static final class PluginEntry<S> {
        private final PluginDescriptor descriptor; // nullable
        private final S instance;

        public PluginEntry(PluginDescriptor descriptor, S instance) {
            this.descriptor = descriptor;
            this.instance = instance;
        }

        /** 返回插件描述符，可能为 null。 */
        public PluginDescriptor getDescriptor() {
            return descriptor;
        }

        public S getInstance() {
            return instance;
        }

        public boolean hasDescriptor() {
            return descriptor != null;
        }
    }
}
