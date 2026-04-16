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
import java.util.Enumeration;
import java.util.Properties;

/**
 * Parsed contents of {@code META-INF/datavines-plugin.properties}.
 */
public final class PluginDescriptor {

    private static final Logger log = LoggerFactory.getLogger(PluginDescriptor.class);

    public static final String DESCRIPTOR_PATH = "META-INF/datavines-plugin.properties";

    private static final String KEY_NAME = "plugin.name";
    private static final String KEY_MODULE = "plugin.module";
    private static final String KEY_VERSION = "plugin.version";
    private static final String KEY_SPI_VERSION = "plugin.spi.version";
    private static final String KEY_MAIN_VERSION_RANGE = "plugin.main.version.range";
    private static final String KEY_DESCRIPTION = "plugin.description";

    private final String pluginName;
    private final String pluginModule;
    private final PluginVersion version;
    private final PluginVersion spiVersion;
    private final String mainVersionRange;
    private final String description;

    private PluginDescriptor(String pluginName, String pluginModule, PluginVersion version,
                             PluginVersion spiVersion, String mainVersionRange,
                             String description) {
        this.pluginName = pluginName;
        this.pluginModule = pluginModule != null ? pluginModule : "";
        this.version = version;
        this.spiVersion = spiVersion;
        this.mainVersionRange = mainVersionRange;
        this.description = description;
    }

    public static PluginDescriptor load(ClassLoader classLoader) {
        try {
            Enumeration<URL> resources = classLoader.getResources(DESCRIPTOR_PATH);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream is = url.openStream()) {
                    Properties props = new Properties();
                    props.load(is);

                    String name = props.getProperty(KEY_NAME);
                    String versionStr = props.getProperty(KEY_VERSION);

                    if (name == null || name.trim().isEmpty()) {
                        log.warn("Missing '{}' in descriptor: {}", KEY_NAME, url);
                        continue;
                    }
                    if (versionStr == null || versionStr.trim().isEmpty()) {
                        log.warn("Missing '{}' in descriptor: {}", KEY_VERSION, url);
                        continue;
                    }

                    PluginVersion version = PluginVersion.of(versionStr.trim());

                    String spiVersionStr = props.getProperty(KEY_SPI_VERSION);
                    PluginVersion spiVersion = (spiVersionStr != null && !spiVersionStr.trim().isEmpty())
                            ? PluginVersion.of(spiVersionStr.trim())
                            : PluginVersion.ZERO;

                    String moduleStr = props.getProperty(KEY_MODULE, "");
                    String mainVersionRange = props.getProperty(KEY_MAIN_VERSION_RANGE, "");
                    String description = props.getProperty(KEY_DESCRIPTION, "");

                    log.debug("Loaded plugin descriptor: {}@{} (module={}) from {}",
                            name.trim(), version, moduleStr.trim(), url);

                    return new PluginDescriptor(
                            name.trim(), moduleStr.trim(), version,
                            spiVersion,
                            mainVersionRange.trim(), description.trim());

                } catch (Exception e) {
                    log.warn("Failed to parse descriptor from {}: {}", url, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan for plugin descriptors: {}", e.getMessage());
        }
        return null;
    }

    public static PluginDescriptor of(String pluginName, String version) {
        return new PluginDescriptor(
                pluginName, "", PluginVersion.of(version),
                PluginVersion.ZERO, "", "");
    }

    public static PluginDescriptor of(String pluginName, String pluginModule, String version,
                                      String spiVersion, String mainVersionRange,
                                      String description) {
        return new PluginDescriptor(
                pluginName, pluginModule != null ? pluginModule : "",
                PluginVersion.of(version),
                PluginVersion.of(spiVersion),
                mainVersionRange != null ? mainVersionRange : "",
                description != null ? description : "");
    }

    public static PluginDescriptor of(String pluginName, String version,
                                      String spiVersion, String mainVersionRange,
                                      String description) {
        return of(pluginName, "", version, spiVersion, mainVersionRange, description);
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getPluginModule() {
        return pluginModule;
    }

    public PluginVersion getVersion() {
        return version;
    }

    public PluginVersion getSpiVersion() {
        return spiVersion;
    }

    public String getPluginId() {
        return pluginName + "@" + version;
    }

    public String getMainVersionRange() {
        return mainVersionRange;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompatibleWith(PluginVersion hostVersion) {
        if (mainVersionRange == null || mainVersionRange.isEmpty()) {
            return true;
        }
        try {
            VersionConstraint constraint = VersionConstraint.parse(mainVersionRange);
            return constraint.matches(hostVersion);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid version range '{}' in plugin {}, treating as compatible",
                    mainVersionRange, getPluginId());
            return true;
        }
    }

    @Override
    public String toString() {
        return "PluginDescriptor{" + getPluginId()
                + (pluginModule.isEmpty() ? "" : ", module=" + pluginModule)
                + ", spiVersion=" + spiVersion
                + ", mainVersionRange='" + mainVersionRange + "'"
                + "}";
    }
}
