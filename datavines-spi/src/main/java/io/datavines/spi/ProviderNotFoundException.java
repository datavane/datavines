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

import java.util.Set;

/**
 * 找不到指定 Key 对应的 Provider 时抛出。
 */
public class ProviderNotFoundException extends RuntimeException {

    private final String registryName;
    private final Object key;

    public ProviderNotFoundException(String registryName, Object key, Set<?> availableKeys) {
        super(String.format(
            "[%s] No provider found for key '%s'. Available keys: %s. "
            + "Ensure the corresponding plugin JAR is on the classpath.",
            registryName, key, availableKeys));
        this.registryName = registryName;
        this.key = key;
    }

    public String getRegistryName() { return registryName; }
    public Object getKey() { return key; }
}
