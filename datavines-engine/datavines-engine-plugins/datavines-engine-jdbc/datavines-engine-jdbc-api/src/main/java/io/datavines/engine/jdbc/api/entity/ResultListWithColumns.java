package io.datavines.engine.jdbc.api.entity;

import java.util.List;
import java.util.Map;

public class ResultListWithColumns extends ResultList {

    private List<QueryColumn> columns;

    public ResultListWithColumns() {

    }

    public ResultListWithColumns(List<QueryColumn> columns, List<Map<String, Object>> resultList){
        super(resultList);
        this.columns = columns;
    }

    public List<QueryColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<QueryColumn> columns) {
        this.columns = columns;
    }
}
