package io.datavines.engine.jdbc.api.utils;

import org.slf4j.Logger;

public class LoggerFactory {

    private static Logger logger;

    public static void setLogger(Logger newLogger){
        logger = newLogger;
    }

    public static Logger getLogger(Class clazz) {
        if (logger != null) {
            return logger;
        }   else {
            return org.slf4j.LoggerFactory.getLogger(clazz);
        }
    }
}
