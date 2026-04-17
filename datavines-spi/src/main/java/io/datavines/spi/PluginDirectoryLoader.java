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
 * Loads plugins from versioned directories.
 *
 * <p>The canonical layout is {@code plugins/{module}/{name}/{version}/}, but the
 * legacy {@code plugins/{name}/{version}/} layout is still accepted.
 */
public final class PluginDirectoryLoader {

    private static final Logger log = LoggerFactory.getLogger(PluginDirectoryLoader.class);

    private final List<Path> pluginRootDirs;
    private final ClassLoader spiClassLoader;
    private final List<String> spiPackages;
    private final PluginVersion currentHostVersion;
    private final String expectedModuleName;

    public PluginDirectoryLoader(List<Path> pluginRootDirs, ClassLoader spiClassLoader) {
        this(pluginRootDirs, spiClassLoader,
                PluginClassLoader.DEFAULT_SPI_PACKAGES, null, null);
    }

    public PluginDirectoryLoader(List<Path> pluginRootDirs,
                                 ClassLoader spiClassLoader,
                                 String expectedModuleName) {
        this(pluginRootDirs, spiClassLoader,
                PluginClassLoader.DEFAULT_SPI_PACKAGES, null, expectedModuleName);
    }

    public PluginDirectoryLoader(List<Path> pluginRootDirs, ClassLoader spiClassLoader,
                                 List<String> spiPackages, PluginVersion currentHostVersion) {
        this(pluginRootDirs, spiClassLoader, spiPackages, currentHostVersion, null);
    }

    public PluginDirectoryLoader(List<Path> pluginRootDirs, ClassLoader spiClassLoader,
                                 List<String> spiPackages, PluginVersion currentHostVersion,
                                 String expectedModuleName) {
        this.pluginRootDirs = Collections.unmodifiableList(new ArrayList<Path>(pluginRootDirs));
        this.spiClassLoader = spiClassLoader;
        this.spiPackages = Collections.unmodifiableList(new ArrayList<String>(spiPackages));
        this.currentHostVersion = currentHostVersion;
        this.expectedModuleName = expectedModuleName;
    }

    public <P> VersionedPluginRegistry<P> load(Class<P> serviceType) {
        return loadInternal(serviceType, null);
    }

    /**
     * Loads an SPI where one provider may expose multiple logical keys.
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

        // Directory names provide fallback metadata. Logical keys always come from the descriptor/provider.
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
        boolean keepClassLoader = false;

        try {
            PluginDescriptor descriptor = PluginDescriptor.load(classLoader);
            if (descriptor == null) {
                log.warn("Missing {} in plugin jars under {}. "
                        + "Using inferred metadata: name={}, version={}",
                        PluginDescriptor.DESCRIPTOR_PATH, versionDir,
                        inferredBundleName, inferredVersion);
                descriptor = PluginDescriptor.of(inferredBundleName, inferredModule, inferredVersion,
                        "0.0.0", "", "");
            }

            validateDescriptor(descriptor, inferredModule, inferredBundleName, inferredVersion);

            if (currentHostVersion != null && !descriptor.isCompatibleWith(currentHostVersion)) {
                log.warn("Plugin {} is not compatible with host version {}. "
                        + "Declared range: {}. Skipping.",
                        descriptor.getPluginId(), currentHostVersion,
                        descriptor.getMainVersionRange());
                return;
            }

            log.info("Loading plugin {} from {}", descriptor.getPluginId(), versionDir);

            ThreadContextClassLoader ctxSwitch = new ThreadContextClassLoader(classLoader);
            try {
                List<P> providers = ServiceLoaderUtils.loadAll(serviceType, classLoader);

                if (providers.isEmpty()) {
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
                    keepClassLoader = true;
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
                keepClassLoader = true;
            } finally {
                ctxSwitch.close();
            }

        } catch (DuplicateProviderException e) {
            log.error("Duplicate plugin detected in {}: {}", versionDir, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to load plugin from {}: {}", versionDir, e.getMessage(), e);
        } finally {
            if (!keepClassLoader) {
                closeQuietly(classLoader);
            }
        }
    }

    /**
     * Scans plugin version directories under all configured roots.
     */
    private List<Path> scanVersionDirs() {
        List<Path> result = new ArrayList<Path>();

        for (Path root : pluginRootDirs) {
            if (!Files.isDirectory(root)) {
                log.warn("Plugin root directory does not exist: {}", root);
                continue;
            }

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
     * Collects all JAR URLs from a plugin version directory.
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
     * Ensures descriptor metadata matches the directory layout being loaded.
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
        if (expectedModuleName != null && !expectedModuleName.trim().isEmpty()) {
            return expectedModuleName;
        }

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
