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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.Assert.assertEquals;

public class PluginDirectoryLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testLoadFromModuleNameVersionDirectory() throws Exception {
        Path pluginsRoot = temporaryFolder.newFolder("plugins").toPath();
        Path versionDir = pluginsRoot.resolve("connector").resolve("mysql").resolve("1.0.0");
        Files.createDirectories(versionDir);
        createPluginJar(versionDir.resolve("mysql.jar"), "connector", "mysql", "1.0.0");

        PluginDirectoryLoader loader = new PluginDirectoryLoader(
                Collections.singletonList(pluginsRoot),
                PluginDirectoryLoaderTest.class.getClassLoader());

        VersionedPluginRegistry<DemoService> registry = loader.load(DemoService.class);

        assertEquals("mysql", registry.getLatest("mysql").name());
    }

    @Test
    public void testLoadFromLegacyNameVersionDirectory() throws Exception {
        Path pluginsRoot = temporaryFolder.newFolder("legacy-plugins").toPath();
        Path versionDir = pluginsRoot.resolve("mysql").resolve("1.0.0");
        Files.createDirectories(versionDir);
        createPluginJar(versionDir.resolve("mysql.jar"), "", "mysql", "1.0.0");

        PluginDirectoryLoader loader = new PluginDirectoryLoader(
                Collections.singletonList(pluginsRoot),
                PluginDirectoryLoaderTest.class.getClassLoader());

        VersionedPluginRegistry<DemoService> registry = loader.load(DemoService.class);

        assertEquals("mysql", registry.getLatest("mysql").name());
    }

    private void createPluginJar(Path jarPath, String module, String name, String version) throws Exception {
        File jarFile = jarPath.toFile();
        JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile));
        try {
            addEntry(jar, PluginDescriptor.DESCRIPTOR_PATH,
                    "plugin.name=" + name + "\n"
                            + "plugin.module=" + module + "\n"
                            + "plugin.version=" + version + "\n"
                            + "plugin.spi.version=1.0.0\n");
            addEntry(jar, "META-INF/services/" + DemoService.class.getName(),
                    DemoPlugin.class.getName() + "\n");
        } finally {
            jar.close();
        }
    }

    private void addEntry(JarOutputStream jar, String name, String content) throws Exception {
        JarEntry entry = new JarEntry(name);
        jar.putNextEntry(entry);
        jar.write(content.getBytes(StandardCharsets.UTF_8));
        jar.closeEntry();
    }

    public interface DemoService {

        String name();
    }

    public static class DemoPlugin implements DemoService {

        @Override
        public String name() {
            return "mysql";
        }
    }
}
