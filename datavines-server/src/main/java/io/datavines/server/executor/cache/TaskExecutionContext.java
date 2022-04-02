package io.datavines.server.executor.cache;

import io.datavines.common.entity.TaskRequest;
import io.datavines.server.executor.runner.TaskRunner;

public class TaskExecutionContext {

    private TaskRequest taskRequest;

    private TaskRunner taskRunner;

    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }

    public TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }
}
