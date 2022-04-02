package io.datavines.common.entity;

public class TaskInfo {

    private Long id;
    private String name;
    private String engineType;
    private String engineParameter;
    private TaskParameter taskParameter;

    public TaskInfo() {
    }

    public TaskInfo(Long id, String name, String engineType, String engineParameter, TaskParameter taskParameter) {
        this.id = id;
        this.name = name;
        this.engineType = engineType;
        this.engineParameter = engineParameter;
        this.taskParameter = taskParameter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getEngineParameter() {
        return engineParameter;
    }

    public void setEngineParameter(String engineParameter) {
        this.engineParameter = engineParameter;
    }

    public TaskParameter getTaskParameter() {
        return taskParameter;
    }

    public void setTaskParameter(TaskParameter taskParameter) {
        this.taskParameter = taskParameter;
    }
}
