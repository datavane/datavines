package io.datavines.common.entity;

/**
 * 
 */
public class LogResult {

    private String msg;

    private int offsetLine;

    public LogResult(String msg, int offsetLine){
        this.msg = msg;
        this.offsetLine = offsetLine;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getOffsetLine() {
        return offsetLine;
    }

    public void setOffsetLine(int offsetLine) {
        this.offsetLine = offsetLine;
    }
}
