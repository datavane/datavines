package io.datavines.metric.api;

import io.datavines.common.utils.StringUtils;
import io.datavines.common.utils.placeholder.PlaceholderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class MetricConstants {

    private static final String SELECT = "select";

    private static final String BASE_SQL =
            "select ${metric_type} as metric_type,"
                    + "${metric_name} as metric_name,"
                    + "${metric_dimension} as metric_dimension,"
                    + "${task_id} as task_id,"
                    + "${actual_value} as actual_value,"
                    + "${expected_value} as expected_value,"
                    + "${expected_type} as expected_type,"
                    + "${check_type} as check_type,"
                    + "${operator} as operator,"
                    + "${threshold} as threshold,"
                    + "${failure_strategy} as failure_strategy,"
//                    + "'${error_output_path}' as error_output_path,"
                    + "${create_time} as create_time,"
                    + "${update_time} as update_time ";

    public static final String DEFAULT_SINK_SQL = BASE_SQL
                    + "from ${actual_table} full join ${expected_table}";

    public static final String MULTI_TABLE_COMPARISON_SINK_SQL = BASE_SQL
                    + "from ( ${actual_execute_sql} ) tmp1 "
                    + "join ( ${expected_execute_sql} ) tmp2";

    public static final String SINGLE_TABLE_CUSTOM_SQL_SINK_SQL = BASE_SQL
                    + "from ( ${actual_table} ) tmp1 "
                    + "join ${expected_table}";

    public static final String TASK_ACTUAL_VALUE_SINK_SQL =
            "select "
                    + "${task_id} as task_id,"
                    + "${metric_name} as metric_name,"
                    + "${unique_code} as unique_code,"
                    + "${actual_value} as actual_value,"
                    + "${data_time} as data_time,"
                    + "${create_time} as create_time,"
                    + "${update_time} as update_time "
                    + "from ${actual_table}";

    public static final List<ColumnInfo> RESULT_COLUMN_LIST = new ArrayList<>();

    public static final List<ColumnInfo> ACTUAL_COLUMN_LIST = new ArrayList<>();

    static {
        RESULT_COLUMN_LIST.add(new ColumnInfo("metric_type",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("metric_name",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("metric_dimension",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("task_id",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("actual_value",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("expected_value",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("expected_type",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("check_type",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("operator",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("threshold",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("failure_strategy",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("create_time",false));
        RESULT_COLUMN_LIST.add(new ColumnInfo("update_time",false));

        ACTUAL_COLUMN_LIST.add(new ColumnInfo("task_id",false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("metric_name",false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("unique_code",false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("actual_value",false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("data_time",false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("create_time",false));
        ACTUAL_COLUMN_LIST.add(new ColumnInfo("update_time",false));
    }
}
