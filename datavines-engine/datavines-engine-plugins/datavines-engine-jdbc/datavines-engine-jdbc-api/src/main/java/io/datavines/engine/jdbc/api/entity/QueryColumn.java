package io.datavines.engine.jdbc.api.entity;

public class QueryColumn {

    private String name;

    private String type;

    private String comment;

    public QueryColumn() {
    }

    public QueryColumn(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setType(String type) {
        this.type = type == null ? "" : type;
    }
}
