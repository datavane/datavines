

package io.datavines.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class TableInfo {

    private String dbName;

    private String tableName;

    private List<String> primaryKeys;

    private List<QueryColumn> columns;

    public TableInfo(String tableName, List<String> primaryKeys, List<QueryColumn> columns) {
        this.tableName = tableName;
        this.primaryKeys = primaryKeys;
        this.columns = columns;
    }

    public TableInfo(String dbName, String tableName, List<String> primaryKeys, List<QueryColumn> columns) {
        this.dbName = dbName;
        this.tableName = tableName;
        this.primaryKeys = primaryKeys;
        this.columns = columns;
    }

    public TableInfo() {
    }
}
