package io.datavines.server.coordinator.server.quartz.cron.impl;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import io.datavines.server.coordinator.repository.entity.JobSchedule;
import io.datavines.server.coordinator.server.quartz.StrategyFactory;
import io.datavines.server.coordinator.server.quartz.cron.FunCron;
import org.springframework.stereotype.Service;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;

@Service
public class MonthCronImpl implements FunCron {

    @Override
    public String funcDeal(JobSchedule jobschedule) {
        CronDefinitionBuilder builder;
        builder = CronDefinitionBuilder.defineCron();
        builder.withDayOfMonth();
        Cron cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
                .withYear(always())
                .withDoW(questionMark())
                .withMonth(always())
                .withDoM(on(1))

                .withHour(on(2))
                .withMinute(on(5))
                .withSecond(on (1))
                .instance();
        return cron.asString();
    }

    @Override
    public String getFuncName(){
        return "";
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        StrategyFactory.register("month", this);
    }

}