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
 * 插件描述符，从 {@code META-INF/datavines-plugin.properties} 加载。
 *
 * <p>描述符声明了插件的名称、版本、SPI 版本兼容性等元数据。
 * 每个插件 JAR 应包含一份描述符文件。
 *
 * <p>属性文件示例：
 * <pre>
 * plugin.name=mysql
 * plugin.version=8.0.33
 * plugin.spi.version=1.0.0
 * plugin.main.version.range=[1.0.0,2.0.0)
 * plugin.description=MySQL connector based on mysql-connector-j 8.0
 * </pre>
 */
public final class PluginDescriptor {

    private static final Logger log = LoggerFactory.getLogger(PluginDescriptor.class);

    public static final String DESCRIPTOR_PATH = "META-INF/datavines-plugin.properties";

    private static final String KEY_NAME = "plugin.name";
    private static final String KEY_VERSION = "plugin.version";
    private static final String KEY_SPI_VERSION = "plugin.spi.version";
    private static final String KEY_MAIN_VERSION_RANGE = "plugin.main.version.range";
    private static final String KEY_DESCRIPTION = "plugin.description";

    private final String pluginName;
    private final PluginVersion version;
    private final PluginVersion spiVersion;
    private final String mainVersionRange;
    private final String description;

    private PluginDescriptor(String pluginName, PluginVersion version,
                             PluginVersion spiVersion, String mainVersionRange,
                             String description) {
        this.pluginName = pluginName;
        this.version = version;
        this.spiVersion = spiVersion;
        this.mainVersionRange = mainVersionRange;
        this.description = description;
    }

    /**
     * 从 ClassLoader 中加载插件描述符。
     *
     * <p>扫描 ClassLoader 可见的所有 {@value DESCRIPTOR_PATH} 资源，
     * 返回第一个成功解析的描述符。
     *
     * @param classLoader 插件的 ClassLoader
     * @return 解析的描述符，若不存在则返回 null
     */
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

                    String mainVersionRange = props.getProperty(KEY_MAIN_VERSION_RANGE, "");
                    String description = props.getProperty(KEY_DESCRIPTION, "");

                    log.debug("Loaded plugin descriptor: {}@{} from {}", name.trim(), version, url);

                    return new PluginDescriptor(
                            name.trim(), version, spiVersion,
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

    /**
     * 手动构建描述符（用于测试或编程式注册）。
     */
    public static PluginDescriptor of(String pluginName, String version) {
        return new PluginDescriptor(
                pluginName, PluginVersion.of(version),
                PluginVersion.ZERO, "", "");
    }

    /**
     * 手动构建描述符（完整参数）。
     */
    public static PluginDescriptor of(String pluginName, String version,
                                      String spiVersion, String mainVersionRange,
                                      String description) {
        return new PluginDescriptor(
                pluginName, PluginVersion.of(version),
                PluginVersion.of(spiVersion),
                mainVersionRange != null ? mainVersionRange : "",
                description != null ? description : "");
    }

    public String getPluginName() {
        return pluginName;
    }

    public PluginVersion getVersion() {
        return version;
    }

    public PluginVersion getSpiVersion() {
        return spiVersion;
    }

    /**
     * 返回插件唯一标识：{name}@{version}，如 "mysql@8.0.33"。
     */
    public String getPluginId() {
        return pluginName + "@" + version;
    }

    public String getMainVersionRange() {
        return mainVersionRange;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查此插件是否与指定的宿主版本兼容。
     *
     * @param hostVersion 宿主（DataVines）版本
     * @return 如果未声明版本范围，返回 true；否则按约束匹配
     */
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
                + ", spiVersion=" + spiVersion
                + ", mainVersionRange='" + mainVersionRange + "'"
                + "}";
    }
}
