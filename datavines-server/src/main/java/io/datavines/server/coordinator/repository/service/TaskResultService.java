package io.datavines.server.coordinator.repository.service;

import io.datavines.server.coordinator.repository.entity.TaskResult;

public interface TaskResultService {

    long insert(TaskResult taskResult);

    int update(TaskResult taskResult);

    int deleteByTaskId(long taskId);

    TaskResult getById(long id);

    TaskResult getByTaskId(long taskId);

}
