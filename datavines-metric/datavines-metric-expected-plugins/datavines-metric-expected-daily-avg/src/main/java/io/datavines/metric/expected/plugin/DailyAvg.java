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
package io.datavines.metric.expected.plugin;

import io.datavines.common.exception.DataVinesException;

import java.util.Map;

import static io.datavines.common.ConfigConstants.METRIC_UNIQUE_KEY;

public class DailyAvg extends AbstractExpectedValue {

    @Override
    public String getName() {
        return "daily_avg";
    }

    @Override
    public String getZhName() {
        return "日均值";
    }

    @Override
    public String getKey(Map<String,String> inputParameter) {
        String uniqueKey = inputParameter.get(METRIC_UNIQUE_KEY);
        return "expected_value_" + uniqueKey;
    }

    @Override
    public String getExecuteSql(Map<String,String> inputParameter) {
        String uniqueKey = inputParameter.get(METRIC_UNIQUE_KEY);
        String engineType = inputParameter.get("engine_type");
        switch (engineType){
            case "spark":
                return getConnectorFactory("spark").getMetricScript().dailyAvg(uniqueKey);
            case "local":
                return getConnectorFactory(inputParameter).getMetricScript().dailyAvg(uniqueKey);
            default:
                throw new DataVinesException(String.format("engine type %s is not supported", engineType));
        }
    }

    @Override
    public String getOutputTable(Map<String,String> inputParameter) {
        String uniqueKey = inputParameter.get(METRIC_UNIQUE_KEY);
        return "daily_range_" + uniqueKey;
    }

    @Override
    public boolean isNeedDefaultDatasource() {
        return true;
    }

    @Override
    public void prepare(Map<String, String> config) {

    }
}
