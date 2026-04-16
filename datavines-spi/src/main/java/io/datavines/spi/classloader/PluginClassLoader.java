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
 * Isolated classloader for a single plugin version.
 *
 * <p>SPI packages are always loaded from the host. Other classes are resolved
 * plugin-first and fall back to the host only when missing locally.
 */
public final class PluginClassLoader extends URLClassLoader {

    /**
     * Packages that must always come from the host classloader.
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
            "com.fasterxml.jackson.annotation.",
            "org.slf4j.",
            "io.opentelemetry.api.",
            "io.opentelemetry.context."
    ));

    private final String pluginId;
    private final ClassLoader spiClassLoader;
    private final List<String> spiPackages;

    public PluginClassLoader(String pluginId, List<URL> urls, ClassLoader spiClassLoader) {
        this(pluginId, urls, spiClassLoader, DEFAULT_SPI_PACKAGES);
    }

    public PluginClassLoader(String pluginId, List<URL> urls,
                             ClassLoader spiClassLoader, List<String> spiPackages) {
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
            Class<?> cached = findLoadedClass(name);
            if (cached != null) {
                return resolveIfNeeded(cached, resolve);
            }

            if (isSpiClass(name)) {
                try {
                    return resolveIfNeeded(spiClassLoader.loadClass(name), resolve);
                } catch (ClassNotFoundException e) {
                    if (existsLocally(name)) {
                        throw new ClassNotFoundException(
                                "SPI class '" + name + "' found in plugin '" + pluginId
                                + "' but not in SPI classloader. "
                                + "Please set the SPI dependency to <scope>provided</scope> in pom.xml.", e);
                    }
                    throw e;
                }
            }

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

    private boolean isSpiClass(String name) {
        for (int i = 0; i < spiPackages.size(); i++) {
            if (name.startsWith(spiPackages.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpiResource(String name) {
        for (int i = 0; i < spiPackages.size(); i++) {
            String pkgPath = spiPackages.get(i).replace('.', '/');
            if (name.startsWith(pkgPath)) {
                return true;
            }
        }
        return false;
    }

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
