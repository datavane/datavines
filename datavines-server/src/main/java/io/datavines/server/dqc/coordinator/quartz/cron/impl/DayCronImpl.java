/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.server.dqc.coordinator.quartz.cron.impl;

import com.cronutils.model.definition.CronDefinitionBuilder;
import io.datavines.common.utils.JSONUtils;
import io.datavines.core.enums.Status;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.api.dto.bo.job.schedule.MapParam;
import io.datavines.server.dqc.coordinator.quartz.cron.StrategyFactory;
import io.datavines.server.dqc.coordinator.quartz.cron.FunCron;

import org.springframework.stereotype.Service;
import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import java.util.Map;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;
import static io.datavines.server.utils.VerificationUtil.verifyIsNeedParam;

@Service
public class DayCronImpl implements FunCron {

    @Override
    public String funcDeal(String param) {
        MapParam mapParam = JSONUtils.parseObject(param,MapParam.class);
        Map<String ,String>   parameter = mapParam.getParameter();
        String[]  times = {"hour", "minute"};
        boolean verify = verifyIsNeedParam(parameter, times);
        if(!verify){
            throw new DataVinesServerException(Status.CREATE_ENV_ERROR);
        }
        String hour = parameter.get("hour");
        String mintute = parameter.get("minute");

        Cron cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
                .withYear(always())
                .withDoW(questionMark())
                .withMonth(always())
                .withDoM(always())
                .withHour(on(Integer.parseInt(hour)))
                .withMinute(on(Integer.parseInt(mintute)))
                .withSecond(on (0))
                .instance();

        return cron.asString();
    }

    @Override
    public String getFuncName(){
        return "day";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        StrategyFactory.register(this.getFuncName(), this);
    }


}
