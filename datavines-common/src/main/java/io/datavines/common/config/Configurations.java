package io.datavines.common.config;

import java.util.Properties;

public class Configurations {

    private final Properties configuration;

    public Configurations() {
        configuration = new Properties();
    }

    public Configurations(Properties configuration) {
        this.configuration = configuration;
    }

    public String getString(String key){
        return configuration.getProperty(key);
    }

    public String getString(String key,String defaultValue){
        return configuration.getProperty(key,defaultValue);
    }

    public int getInt(String key){
        return Integer.parseInt(configuration.getProperty(key));
    }

    public int getInt(String key,String defaultValue) {
        return Integer.parseInt(configuration.getProperty(key,defaultValue));
    }

    public int getInt(String key,Integer defaultValue) {
        return Integer.parseInt(configuration.getProperty(key,String.valueOf(defaultValue)));
    }

    public Float getFloat(String key) {
        return Float.valueOf(configuration.getProperty(key));
    }

    public Float getFloat(String key,String defaultValue) {
        return Float.valueOf(configuration.getProperty(key,defaultValue));
    }
}
