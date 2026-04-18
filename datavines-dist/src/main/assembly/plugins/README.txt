DataVines Plugin Directory
==========================

DataVines uses a versioned plugin directory layout so that multiple versions of
the same connector or metric can coexist at runtime, each loaded by its own
isolated ClassLoader.

Directory layout
----------------

    plugins/
    ├── {module}/               # connector | metric | engine | notification | registry | ...
    │   └── {name}/              # value of plugin.name
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

Example — connector and registry plugins:

    plugins/
    ├── connector/
    │   └── mysql/
    │       └── 1.0.0-SNAPSHOT/
    │           ├── datavines-connector-mysql-1.0.0-SNAPSHOT.jar
    │           └── mysql-connector-j-*.jar
    └── registry/
        └── mysql/
            └── 1.0.0-SNAPSHOT/
                └── datavines-registry-mysql-1.0.0-SNAPSHOT.jar

Notes
-----
* In a packaged deployment these directories are populated automatically by the
  plugin module's classifier=plugin zip. Do not add plugin JARs to libs/
  manually.
* Plugin-private dependencies are controlled by Maven scopes in each plugin
  module. Use provided for host APIs/shared libraries; use compile/runtime for
  JARs that must live beside the plugin implementation.
* When the plugins/ directory is absent, the server falls back to loading
  plugins from the application classpath via ServiceLoader. This requires the
  plugin implementation modules/JARs to be present on that classpath.
* Override the plugin root: -Ddatavines.plugins.dir=/path/to/plugins
