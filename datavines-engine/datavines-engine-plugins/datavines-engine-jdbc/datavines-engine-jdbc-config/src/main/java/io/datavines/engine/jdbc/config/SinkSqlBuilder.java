package io.datavines.engine.jdbc.config;

import io.datavines.common.utils.StringUtils;
import io.datavines.metric.api.ColumnInfo;
import io.datavines.metric.api.MetricConstants;

import java.util.ArrayList;
import java.util.List;

public class SinkSqlBuilder {

    public static String getTaskResultSql() {

        List<String> columnList = new ArrayList<>();
        List<String> columnValueList = new ArrayList<>();
        for (ColumnInfo columnInfo : MetricConstants.RESULT_COLUMN_LIST) {

            columnList.add(columnInfo.getName());

            if (columnInfo.isNeedSingleQuotation()) {
                columnValueList.add(StringUtils.wrapperSingleQuotes("${"+columnInfo.getName()+"}"));
            } else {
                columnValueList.add("${"+columnInfo.getName()+"}");
            }

        }

        return "INSERT INTO task_result ("
                + String.join(", ", columnList)+") VALUES ("
                + String.join(", ", columnValueList)+ ")";
    }

    public static String getActualValueSql() {

        List<String> columnList = new ArrayList<>();
        List<String> columnValueList = new ArrayList<>();
        for (ColumnInfo columnInfo : MetricConstants.ACTUAL_COLUMN_LIST) {

            columnList.add(columnInfo.getName());

            if (columnInfo.isNeedSingleQuotation()) {
                columnValueList.add("${"+ StringUtils.wrapperSingleQuotes(columnInfo.getName())+"}");
            } else {
                columnValueList.add("${"+columnInfo.getName()+"}");
            }

        }

        return "INSERT INTO actual_values ("
                + String.join(", ", columnList)+") VALUES ("
                + String.join(", ", columnValueList)+ ")";
    }
}
