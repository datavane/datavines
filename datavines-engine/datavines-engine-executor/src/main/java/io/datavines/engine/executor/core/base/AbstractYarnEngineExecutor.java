package io.datavines.engine.executor.core.base;

import io.datavines.common.utils.YarnUtils;
import io.datavines.engine.executor.core.executor.ShellCommandProcess;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;


public abstract class AbstractYarnEngineExecutor extends AbstractEngineExecutor {

    protected ShellCommandProcess shellCommandProcess;

    @Override
    public void cancel() throws Exception {

        cancel = true;
        // cancel process
        shellCommandProcess.cancel();

        killYarnApplication();

    }

    private void killYarnApplication() {

        try {
            String applicationId = YarnUtils.getYarnAppId(taskRequest.getTenantCode(), taskRequest.getTaskUniqueId());

            if (StringUtils.isNotEmpty(applicationId)) {
                // sudo -u user command to run command
                String cmd = String.format("sudo -u %s yarn application -kill %s", taskRequest.getTenantCode(), applicationId);

                logger.info("yarn application -kill {}", applicationId);

                Runtime.getRuntime().exec(cmd);
            }

        } catch (IOException e) {
            logger.info("kill attempt failed." + e.getMessage(), e);
        }

    }
}
