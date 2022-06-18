package io.datavines.server.coordinator.server.quartz.cron.impl;

import com.cronutils.model.definition.CronDefinitionBuilder;
import io.datavines.common.utils.JSONUtils;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.MapParam;
import io.datavines.server.coordinator.repository.entity.JobSchedule;
import io.datavines.server.coordinator.server.quartz.StrategyFactory;
import io.datavines.server.coordinator.server.quartz.cron.FunCron;

import org.springframework.stereotype.Service;
import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import org.quartz.CronExpression;

import java.util.Map;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

@Service
public class DayCronImpl implements FunCron {
    @Override
    public String funcDeal(JobSchedule jobschedule) {
        String param = jobschedule.getParam();
        MapParam mapParam = JSONUtils.parseObject(param,MapParam.class);
        Map<String ,String>   parameter = mapParam.getParameter();
        String mintute =parameter.get("minute");
      //  String second =parameter.get("second");

        Cron cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
                .withYear(always())
                .withDoW(questionMark())
                .withMonth(always())
                .withDoM(always())
                .withHour(on(23))
                .withMinute(on(Integer.parseInt(mintute)))
                .withSecond(on (0))
                .instance();
        System.out.println(cron.asString());

        return cron.asString();
    }

    @Override
    public String getFuncName(){
        return "";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        StrategyFactory.register(1, this);
    }

}
