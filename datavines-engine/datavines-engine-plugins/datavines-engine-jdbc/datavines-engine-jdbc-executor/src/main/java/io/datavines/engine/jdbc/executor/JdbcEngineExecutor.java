package io.datavines.engine.jdbc.executor;

import io.datavines.common.config.Configurations;
import io.datavines.common.entity.ProcessResult;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.utils.LoggerUtils;
import io.datavines.engine.executor.core.base.AbstractEngineExecutor;
import io.datavines.engine.jdbc.core.JdbcDataVinesBootstrap;
import org.slf4j.Logger;

public class JdbcEngineExecutor extends AbstractEngineExecutor {

    private Configurations configurations;

    private JdbcDataVinesBootstrap bootstrap;

    @Override
    public void init(TaskRequest taskRequest, Logger logger, Configurations configurations) throws Exception {
        String threadLoggerInfoName = String.format(LoggerUtils.TASK_LOG_INFO_FORMAT, taskRequest.getTaskUniqueId());
        Thread.currentThread().setName(threadLoggerInfoName);

        this.taskRequest = taskRequest;
        this.logger = logger;
        this.configurations = configurations;
    }

    @Override
    public void execute() throws Exception {
        String[] args = new String[1];
        args[0] = taskRequest.getApplicationParameter();
        bootstrap = new JdbcDataVinesBootstrap();
        this.processResult = bootstrap.execute(args);
    }

    @Override
    public void after() throws Exception {

    }

    @Override
    public void cancel() throws Exception {
        if(bootstrap != null){
            bootstrap.stop();
        }
    }

    @Override
    public ProcessResult getProcessResult() {
        return this.processResult;
    }

    @Override
    public TaskRequest getTaskRequest() {
        return this.taskRequest;
    }

    @Override
    protected String buildCommand() {
        return null;
    }
}
