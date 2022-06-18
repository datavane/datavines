package io.datavines.server.coordinator.server.quartz;

import io.datavines.server.coordinator.server.quartz.cron.FunCron;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class StrategyFactory {


    private static Map<Integer, FunCron> services = new ConcurrentHashMap<>();

    public static FunCron getByNum(int type) {
        return services.get(type);
    }

    public static void register(int type, FunCron cronService) {
        Assert.notNull(type, "type can't be null");
        services.put(type, cronService);
    }

}
