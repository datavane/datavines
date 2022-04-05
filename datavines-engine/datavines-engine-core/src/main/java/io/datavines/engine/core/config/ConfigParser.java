package io.datavines.engine.core.config;

import io.datavines.common.config.Config;
import io.datavines.common.config.ConfigRuntimeException;
import io.datavines.common.config.DataVinesQualityConfig;
import io.datavines.common.config.EnvConfig;
import io.datavines.engine.api.component.Component;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.core.utils.JsonUtils;
import io.datavines.spi.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(ConfigParser.class);

    private final String configFile;

    private final DataVinesQualityConfig config;

    private final EnvConfig envConfig;

    private final RuntimeEnvironment env;

    public ConfigParser(String configFile){
        this.configFile = configFile;
        this.config = load();
        this.envConfig = config.getEnvConfig();
        this.env = createRuntimeEnvironment();
    }

    private DataVinesQualityConfig load() {

        if (configFile.isEmpty()) {
            throw new ConfigRuntimeException("Please specify config file");
        }

        logger.info("Loading config file: " + configFile);

        DataVinesQualityConfig config = JsonUtils.parseObject(configFile, DataVinesQualityConfig.class);

        logger.info("config after parse: " + JsonUtils.toJsonString(config));

        return config;
    }

    private RuntimeEnvironment createRuntimeEnvironment() {
        RuntimeEnvironment env = PluginLoader
                .getPluginLoader(RuntimeEnvironment.class)
                .getNewPlugin(envConfig.getEngine());
        Config config = new Config(envConfig.getConfig());
        config.put("type",envConfig.getType());
        env.setConfig(config);
        env.prepare();
        return env;
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return env;
    }

    public List<Component> getSourcePlugins() {
        List<Component> sourcePluginList = new ArrayList<>();
        config.getSourceParameters().forEach(sourceConfig -> {
            String pluginName = envConfig.getEngine() + "-" + sourceConfig.getPlugin()+"-source";
            Component component = PluginLoader
                    .getPluginLoader(Component.class)
                    .getNewPlugin(pluginName);
            sourceConfig.getConfig().put("plugin_type",sourceConfig.getType());
            component.setConfig(new Config(sourceConfig.getConfig()));
            sourcePluginList.add(component);
        });
        return sourcePluginList;
    }

    public List<Component> getSinkPlugins() {
        List<Component> sinkPluginList = new ArrayList<>();
        config.getSinkParameters().forEach(sinkConfig -> {
            String pluginName = envConfig.getEngine() + "-" + sinkConfig.getPlugin()+"-sink";
            Component component = PluginLoader
                    .getPluginLoader(Component.class)
                    .getNewPlugin(pluginName);
            sinkConfig.getConfig().put("plugin_type", sinkConfig.getType());
            component.setConfig(new Config(sinkConfig.getConfig()));
            sinkPluginList.add(component);
        });
        return sinkPluginList;
    }

    public List<Component> getTransformPlugins() {
        List<Component> transformPluginList = new ArrayList<>();
        config.getTransformParameters().forEach(transformConfig -> {
            String pluginName = envConfig.getEngine() + "-" + transformConfig.getPlugin() + "-transform";
            Component component = PluginLoader
                    .getPluginLoader(Component.class)
                    .getNewPlugin(pluginName);
            transformConfig.getConfig().put("plugin_type", transformConfig.getType());
            component.setConfig(new Config(transformConfig.getConfig()));
            transformPluginList.add(component);
        });
        return transformPluginList;
    }
}
