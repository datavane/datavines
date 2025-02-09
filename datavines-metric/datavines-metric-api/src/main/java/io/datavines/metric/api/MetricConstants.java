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

import java.util.ArrayList;
import java.util.List;

public class MetricConstants {

    public static final List<ColumnInfo> RESULT_COLUMN_LIST = new ArrayList<>();

    public static final List<ColumnInfo> ACTUAL_COLUMN_LIST = new ArrayList<>();

    public static final List<ColumnInfo> PROFILE_COLUMN_LIST = new ArrayList<>();

    static {
        RESULT_COLUMN_LIST.add(new ColumnInfo("job_execution_id",false, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("metric_unique_key",true, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("metric_type",false, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("metric_name",false, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("metric_dimension",false, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("database_name",true, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("table_name",true, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("column_name",true, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("actual_value",false, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("expected_value",false, true));
        RESULT_COLUMN_LIST.add(new ColumnInfo("expected_type",false, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("result_formula",true, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("operator",true, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("threshold",false, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("create_time",false, false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("update_time",false, false));

        ACTUAL_COLUMN_LIST.add(new ColumnInfo("job_execution_id",false, false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("metric_name",false, false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("unique_code",false, false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("actual_value",false, false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("data_time",false, false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("create_time",false, false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("update_time",false, false));

        PROFILE_COLUMN_LIST.add(new ColumnInfo("entity_uuid",true, false));
        PROFILE_COLUMN_LIST.add(new ColumnInfo("metric_name",false, false));
        PROFILE_COLUMN_LIST.add(new ColumnInfo("actual_value",true, false));
        PROFILE_COLUMN_LIST.add(new ColumnInfo("actual_value_type",true, false));
        PROFILE_COLUMN_LIST.add(new ColumnInfo("data_date",false, false));
        PROFILE_COLUMN_LIST.add(new ColumnInfo("update_time",false, false));
    }
}
