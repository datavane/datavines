package io.datavines.metric.api;

public class ColumnInfo {

    private String name;
    private boolean needSingleQuotation;

    public ColumnInfo(String name, boolean needSingleQuotation) {
        this.name = name;
        this.needSingleQuotation = needSingleQuotation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNeedSingleQuotation() {
        return needSingleQuotation;
    }

    public void setNeedSingleQuotation(boolean needSingleQuotation) {
        this.needSingleQuotation = needSingleQuotation;
    }
}
