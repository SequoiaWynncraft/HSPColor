package net.warze.hspcolor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {
    public static Logger LOGGER;

    public static void init(){
        LOGGER = LoggerFactory.getLogger("hspcolor");
    }
    public static void info(String s){
        LOGGER.info(s);
    }
    public static void warn(String s){
        LOGGER.warn(s);
    }
    public static void error(String s){
        LOGGER.error(s);
    }
}
