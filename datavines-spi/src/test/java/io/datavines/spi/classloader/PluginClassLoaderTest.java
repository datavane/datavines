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

import io.datavines.spi.PluginDescriptor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PluginClassLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testNonSpiClassUsesPluginCopyFirst() throws Exception {
        Path parentClasses = compileClass("example.shared.Duplicate", "parent");
        Path pluginClasses = compileClass("example.shared.Duplicate", "plugin");

        try (URLClassLoader parentLoader = new URLClassLoader(
                new URL[]{parentClasses.toUri().toURL()},
                PluginClassLoaderTest.class.getClassLoader());
             PluginClassLoader pluginLoader = new PluginClassLoader(
                     "duplicate@1.0.0",
                     Collections.singletonList(pluginClasses.toUri().toURL()),
                     parentLoader,
                     Collections.singletonList("example.spi."))) {

            Class<?> loaded = pluginLoader.loadClass("example.shared.Duplicate");
            Object instance = loaded.getDeclaredConstructor().newInstance();

            assertSame(pluginLoader, loaded.getClassLoader());
            assertEquals("plugin", loaded.getMethod("source").invoke(instance));
        }
    }

    @Test
    public void testSpiClassAlwaysUsesParentCopy() throws Exception {
        Path parentClasses = compileClass("example.spi.SharedType", "parent");
        Path pluginClasses = compileClass("example.spi.SharedType", "plugin");

        try (URLClassLoader parentLoader = new URLClassLoader(
                new URL[]{parentClasses.toUri().toURL()},
                PluginClassLoaderTest.class.getClassLoader());
             PluginClassLoader pluginLoader = new PluginClassLoader(
                     "shared@1.0.0",
                     Collections.singletonList(pluginClasses.toUri().toURL()),
                     parentLoader,
                     Collections.singletonList("example.spi."))) {

            Class<?> loaded = pluginLoader.loadClass("example.spi.SharedType");
            Object instance = loaded.getDeclaredConstructor().newInstance();

            assertSame(parentLoader, loaded.getClassLoader());
            assertEquals("parent", loaded.getMethod("source").invoke(instance));
        }
    }

    @Test
    public void testMissingParentSpiClassFailsFast() throws Exception {
        Path parentClasses = temporaryFolder.newFolder("empty-parent").toPath();
        Path pluginClasses = compileClass("example.spi.MissingType", "plugin");

        try (URLClassLoader parentLoader = new URLClassLoader(
                new URL[]{parentClasses.toUri().toURL()},
                PluginClassLoaderTest.class.getClassLoader());
             PluginClassLoader pluginLoader = new PluginClassLoader(
                     "missing@1.0.0",
                     Collections.singletonList(pluginClasses.toUri().toURL()),
                     parentLoader,
                     Collections.singletonList("example.spi."))) {

            try {
                pluginLoader.loadClass("example.spi.MissingType");
                fail("Expected ClassNotFoundException");
            } catch (ClassNotFoundException e) {
                assertTrue(e.getMessage().contains(
                        "Please set the SPI dependency to <scope>provided</scope>"));
            }
        }
    }

    @Test
    public void testPluginDescriptorPrefersPluginResourceFirst() throws Exception {
        Path parentRoot = temporaryFolder.newFolder("parent-resources").toPath();
        Path pluginRoot = temporaryFolder.newFolder("plugin-resources").toPath();

        writeDescriptor(parentRoot, "parent-plugin", "1.0.0");
        writeDescriptor(pluginRoot, "plugin-plugin", "2.0.0");

        try (URLClassLoader parentLoader = new URLClassLoader(
                new URL[]{parentRoot.toUri().toURL()},
                PluginClassLoaderTest.class.getClassLoader());
             PluginClassLoader pluginLoader = new PluginClassLoader(
                     "resource@1.0.0",
                     Collections.singletonList(pluginRoot.toUri().toURL()),
                     parentLoader,
                     Collections.<String>emptyList())) {

            PluginDescriptor descriptor = PluginDescriptor.load(pluginLoader);

            assertNotNull(descriptor);
            assertEquals("plugin-plugin", descriptor.getPluginName());
            assertEquals("2.0.0", descriptor.getVersion().toString());
        }
    }

    private Path compileClass(String className, String sourceValue) throws Exception {
        Path root = temporaryFolder.newFolder(className.replace('.', '_') + "_" + sourceValue).toPath();
        Path sourceFile = root.resolve(className.replace('.', '/') + ".java");
        Files.createDirectories(sourceFile.getParent());

        int lastDot = className.lastIndexOf('.');
        String packageName = className.substring(0, lastDot);
        String simpleName = className.substring(lastDot + 1);
        String source = "package " + packageName + ";\n"
                + "public class " + simpleName + " {\n"
                + "    public String source() {\n"
                + "        return \"" + sourceValue + "\";\n"
                + "    }\n"
                + "}\n";
        Files.write(sourceFile, source.getBytes(StandardCharsets.UTF_8));

        Path classesDir = root.resolve("classes");
        Files.createDirectories(classesDir);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull("JDK compiler is required for PluginClassLoaderTest", compiler);
        int result = compiler.run(null, null, null,
                "-d", classesDir.toString(),
                sourceFile.toString());
        assertEquals("Compilation failed for " + className, 0, result);
        return classesDir;
    }

    private void writeDescriptor(Path root, String pluginName, String version) throws Exception {
        Path descriptor = root.resolve(PluginDescriptor.DESCRIPTOR_PATH);
        Files.createDirectories(descriptor.getParent());
        String content = "plugin.name=" + pluginName + "\n"
                + "plugin.version=" + version + "\n";
        Files.write(descriptor, content.getBytes(StandardCharsets.UTF_8));
    }
}
