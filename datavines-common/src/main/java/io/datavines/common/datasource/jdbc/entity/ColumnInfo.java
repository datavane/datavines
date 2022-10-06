package io.datavines.common.datasource.jdbc.entity;

import lombok.Data;

@Data
public class ColumnInfo {

    private String name;

    private String type;

    private String comment;

    private boolean isPrimaryKey;

    public ColumnInfo() {
    }

    public ColumnInfo(String name, String type) {
        this(name, type, null, false);
    }

    public ColumnInfo(String name, String type, String comment,boolean isPrimaryKey) {
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.isPrimaryKey = isPrimaryKey;
    }

}
