package io.datavines.engine.jdbc.api.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultList implements Serializable {
    protected List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

    public ResultList() {
    }

    public ResultList(List<Map<String, Object>> resultList) {
        this.resultList = resultList;
    }

    public List<Map<String, Object>> getResultList() {
        return resultList;
    }

    public void setResultList(List<Map<String, Object>> resultList) {
        this.resultList = resultList;
    }
}
