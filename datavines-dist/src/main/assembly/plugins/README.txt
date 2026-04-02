DataVines Versioned Plugin Directory
=====================================

Place versioned plugin JARs in subdirectories structured as:

    plugins/{plugin-name}/{version}/*.jar

Each plugin JAR MUST contain META-INF/datavines-plugin.properties:

    plugin.name=mysql
    plugin.version=8.0.33
    plugin.spi.version=1.0.0
    plugin.main.version.range=[1.0.0,2.0.0)
    plugin.description=MySQL connector

Example — deploying two versions of the MySQL connector simultaneously:

    plugins/
    ├── mysql/
    │   ├── 5.7.44/
    │   │   ├── datavines-connector-mysql-5.7.44.jar
    │   │   └── mysql-connector-j-5.1.49.jar
    │   └── 8.0.33/
    │       ├── datavines-connector-mysql-8.0.33.jar
    │       └── mysql-connector-j-8.0.33.jar
    └── postgresql/
        └── 42.7.0/
            └── datavines-connector-postgresql.jar

When this directory contains versioned subdirectories, DataVines activates
"directory mode" with ClassLoader isolation per plugin version (production).

When this directory is empty or absent, DataVines uses "classpath mode" —
all plugins in libs/ are discovered via ServiceLoader (suitable for IDE
development or single-version deployments).

To override the plugins directory path, set the JVM property:
    -Ddatavines.plugins.dir=/absolute/path/to/plugins
