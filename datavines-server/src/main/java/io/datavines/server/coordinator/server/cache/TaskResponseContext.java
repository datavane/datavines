package io.datavines.server.coordinator.server.cache;

import io.datavines.common.entity.TaskRequest;
import io.datavines.server.command.CommandCode;

public class TaskResponseContext {

    private CommandCode commandCode;

    private TaskRequest taskRequest;

    public TaskResponseContext(CommandCode commandCode, TaskRequest taskRequest) {
        this.commandCode = commandCode;
        this.taskRequest = taskRequest;
    }

    public CommandCode getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(CommandCode commandCode) {
        this.commandCode = commandCode;
    }

    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }
}
