package io.datavines.engine.api.engine;

import org.slf4j.Logger;

import io.datavines.common.config.Configurations;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.entity.ProcessResult;
import io.datavines.spi.SPI;;

@SPI
public interface EngineExecutor {

    /**
     * 进行初始化操作
     * @throws Exception Exception
     */
    void init(TaskRequest taskRequest, Logger logger, Configurations configurations) throws Exception;

    /**
     * 执行实际内容
     * @throws Exception Exception
     */
    void execute() throws Exception;

    /**
     * 做好任务执行完之后的处理工作
     * @throws Exception Exception
     */
    void after() throws Exception;

    /**
     * 取消任务
     * @throws Exception Exception
     */
    void cancel() throws Exception;

    /**
     * 是否取消
     * @throws Exception Exception
     */
    boolean isCancel() throws Exception;

    /**
     * 获取执行结果
     */
    ProcessResult getProcessResult();

    /**
     * 获取execution job
     * @return
     */
    TaskRequest getTaskRequest();
}
