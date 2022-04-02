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

package io.datavines.metric.api;

import java.util.List;
import java.util.Map;

import io.datavines.common.config.CheckResult;
import io.datavines.common.entity.ExecuteSql;
import io.datavines.spi.SPI;;

@SPI
public interface SqlMetric {

    String getName();

    MetricDimension getDimension();

    MetricType getType();

    /**
     * the middle sql thar calculate invalidate items
     * @return string
     */
    String getInvalidateItemsSql();

    boolean isInvalidateItemsCanOutput();

    String getActualValueSql();

    /**
     * get invalidate items execute sql
     * @return ExecuteSql
     */
    default ExecuteSql getInvalidateItems() {
        ExecuteSql executeSql = new ExecuteSql();
        executeSql.setResultTable("invalidate_items");
        executeSql.setSql(getInvalidateItemsSql());
        executeSql.setErrorOutput(isInvalidateItemsCanOutput());
        return executeSql;
    }

    /**
     * get actual value execute sql
     * @return ExecuteSql
     */
    default ExecuteSql getActualValue() {
        ExecuteSql executeSql = new ExecuteSql();
        executeSql.setResultTable("invalidate_count");
        executeSql.setSql(getActualValueSql());
        executeSql.setErrorOutput(false);
        return executeSql;
    }

    /**
     * get actual name
     * @return
     */
    default String getActualName() {
        return "actual_name";
    }

    CheckResult validateConfig(Map<String,String> config);

    List<String> getConfigList();

    void prepare(Map<String,String> config);
}
