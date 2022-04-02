package io.datavines.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.locks.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置加载工具
 * 
 */
public class ConfigUtils {

    private static final Logger logger =
            LoggerFactory.getLogger(Condition.class);

    public static Properties loadConfigurationFile(String configPath) throws IOException, IllegalArgumentException {
        File configFile = new File(configPath);

        if(!configFile.exists()){
            throw new IllegalArgumentException("config file " + configPath + " doesn't exist......");
        }

        Properties configProperties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            configProperties.load(inputStream);
        }

        logger.info("load configuration from file successfully");

        return configProperties;
    }

}
