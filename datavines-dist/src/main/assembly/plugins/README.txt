DataVines Plugin Directory
==========================

DataVines uses a versioned plugin directory layout so that multiple versions of
the same connector or metric can coexist at runtime, each loaded by its own
isolated ClassLoader.

Directory layout
----------------

    plugins/
    ├── {module}/               # connector | metric | engine | notification | registry | ...
    │   └── {plugin-name}/
    │       └── {version}/
    │           ├── datavines-{plugin}.jar
    │           └── {optional-driver}.jar

Each plugin JAR must include META-INF/datavines-plugin.properties:

    plugin.name=mysql
    plugin.module=connector
    plugin.version=1.0.0
    plugin.spi.version=1.0.0
    plugin.main.version.range=[1.0.0,2.0.0)
    plugin.description=MySQL connector

    # Optional: extra JARs to co-locate (JDBC drivers, etc.)
    plugin.dependencies=com.mysql:mysql-connector-j

Example — two MySQL driver versions side by side:

    plugins/
    └── connector/
        └── mysql/
            ├── 5.x/
            │   ├── datavines-connector-mysql.jar
            │   └── mysql-connector-j-5.1.49.jar
            └── 8.x/
                ├── datavines-connector-mysql.jar
                └── mysql-connector-j-8.4.0.jar

Notes
-----
* In a packaged deployment these directories are populated automatically by the
  build. Do not add plugin JARs to libs/ manually.
* When the plugins/ directory is absent (e.g. IDE run), the server falls back to
  loading plugins from the application classpath via ServiceLoader.
* Override the plugin root: -Ddatavines.plugins.dir=/path/to/plugins
