package io.datavines.server.coordinator.repository.service;

import java.util.List;

import io.datavines.common.enums.ExecutionStatus;
import io.datavines.server.coordinator.api.dto.task.SubmitTask;
import io.datavines.server.coordinator.repository.entity.Task;

public interface TaskService {

    /**
     * 返回主键字段id值
     * @param task
     * @return
     */
    long insert(Task task);

    /**
     * updateById
     * @param task
     * @return
     */
    int update(Task task);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    Task getById(long id);

    /**
     * 根据dataSourceId获取task列表
     * @param dataSourceId
     * @return
     */
    List<Task> listByDataSourceId(long dataSourceId);

    Long submitTask(SubmitTask submitTask);

    Long killTask(Long taskId);

    List<Task> listNeedFailover(String host);

    List<Task> listTaskNotInServerList(List<String> hostList);

}
