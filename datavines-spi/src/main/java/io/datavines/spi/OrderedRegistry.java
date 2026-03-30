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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 列表型注册表：多个 Provider 共存，按优先级排序。
 *
 * <p>初始化完成后不可变，天然线程安全。
 *
 * @param <P> Provider 类型
 */
public final class OrderedRegistry<P> {

    private static final Logger log = LoggerFactory.getLogger(OrderedRegistry.class);

    private final List<P> providers;

    private OrderedRegistry(List<P> providers) {
        this.providers = Collections.unmodifiableList(providers);
    }

    /**
     * 从 ServiceLoader 加载并按指定比较器排序。
     */
    public static <P> OrderedRegistry<P> load(
            Class<P> providerType,
            Comparator<P> ordering) {
        return from(ServiceLoaderUtils.loadAll(providerType), ordering, providerType.getSimpleName());
    }

    /**
     * 从显式列表构建（测试场景）。
     */
    @SafeVarargs
    public static <P> OrderedRegistry<P> of(Comparator<P> ordering, P... providers) {
        return from(Arrays.asList(providers), ordering, "test");
    }

    /**
     * 从可迭代集合构建。
     */
    public static <P> OrderedRegistry<P> from(
            Iterable<P> providers,
            Comparator<P> ordering,
            String registryName) {
        List<P> sorted = StreamSupport.stream(providers.spliterator(), false)
            .sorted(ordering)
            .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            log.info("[{}] Registered [order={}]: {}",
                registryName, i, sorted.get(i).getClass().getName());
        }

        log.info("[{}] Initialized with {} providers", registryName, sorted.size());
        return new OrderedRegistry<>(sorted);
    }

    /**
     * 获取所有 Provider（按优先级排序）。
     */
    public List<P> getAll() {
        return providers;
    }

    public boolean isEmpty() {
        return providers.isEmpty();
    }

    public int size() {
        return providers.size();
    }
}
