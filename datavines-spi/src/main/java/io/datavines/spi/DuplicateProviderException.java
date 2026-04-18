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

/**
 * Raised when multiple providers register the same logical key.
 */
public class DuplicateProviderException extends RuntimeException {

    private final String registryName;
    private final Object key;

    public DuplicateProviderException(String registryName, Object key,
                                      String existingClass, String conflictingClass) {
        super(String.format(
            "[%s] Duplicate provider for key '%s': [%s] and [%s]. "
            + "Check classpath for duplicate JARs.",
            registryName, key, existingClass, conflictingClass));
        this.registryName = registryName;
        this.key = key;
    }

    public String getRegistryName() { return registryName; }
    public Object getKey() { return key; }
}
