package io.datavines.engine.config.entity;

import io.datavines.common.entity.TaskInfo;
import java.util.Map;

public class ConfigRequest {
    private Map<String, String> inputParameter;
    private TaskInfo taskInfo;

    public ConfigRequest() {
    }

    public ConfigRequest(Map<String, String> inputParameter, TaskInfo taskInfo) {
        this.inputParameter = inputParameter;
        this.taskInfo = taskInfo;
    }

    public Map<String, String> getInputParameter() {
        return inputParameter;
    }

    public void setInputParameter(Map<String, String> inputParameter) {
        this.inputParameter = inputParameter;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }
}
