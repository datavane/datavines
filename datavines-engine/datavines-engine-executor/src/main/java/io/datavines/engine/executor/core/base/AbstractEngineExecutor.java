package io.datavines.engine.executor.core.base;

import java.util.List;

import org.slf4j.Logger;

import io.datavines.common.entity.TaskRequest;
import io.datavines.common.entity.ProcessResult;
import io.datavines.common.parameter.AbstractParameters;
import io.datavines.engine.api.engine.EngineExecutor;

public abstract class AbstractEngineExecutor implements EngineExecutor {

    protected TaskRequest taskRequest;

    protected Logger logger;

    protected volatile boolean cancel = false;

    protected ProcessResult processResult;

    public AbstractEngineExecutor() {

    }

    /**
     * log handle
     * @param logs log list
     */
    public void logHandle(List<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        logger.info(" -> {}", String.join("\n\t", logs));
    }

    @Override
    public boolean isCancel() throws Exception {
        return cancel;
    }

    protected abstract String buildCommand();
}
