package io.datavines.server.catalog;

import io.datavines.common.entity.TaskRequest;
import io.datavines.common.utils.*;
import io.datavines.server.dqc.coordinator.cache.TaskExecuteManager;
import io.datavines.server.enums.CommandType;
import io.datavines.server.registry.Register;
import io.datavines.server.repository.entity.Command;
import io.datavines.server.repository.entity.Task;
import io.datavines.server.repository.entity.catalog.CatalogCommand;
import io.datavines.server.repository.entity.catalog.CatalogTask;
import io.datavines.server.repository.service.impl.JobExternalService;
import io.datavines.server.utils.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;

import static io.datavines.common.CommonConstants.SLEEP_TIME_MILLIS;
import static io.datavines.common.utils.CommonPropertyUtils.*;

@Slf4j
public class CatalogTaskScheduler extends Thread {

    private final String CATALOG_TASK_LOCK_KEY =
            CommonPropertyUtils.getString(CommonPropertyUtils.CATALOG_TASK_LOCK_KEY, CommonPropertyUtils.CATALOG_TASK_LOCK_KEY_DEFAULT);

    private static final int[] RETRY_BACKOFF = {1, 2, 3, 5, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10};

    private final JobExternalService jobExternalService;

    private final CatalogTaskManager catalogTaskManager;

    private final Register register;

    public CatalogTaskScheduler(CatalogTaskManager catalogTaskManager, Register register){
        this.jobExternalService = SpringApplicationContext.getBean(JobExternalService.class);
        this.catalogTaskManager = catalogTaskManager;
        this.register = register;
    }

    @Override
    public void run() {
        log.info("catalog task scheduler started");

        int retryNum = 0;
        while (Stopper.isRunning()) {
            CatalogCommand command = null;
            try {
                boolean runCheckFlag = OSUtils.checkResource(
                        CommonPropertyUtils.getDouble(MAX_CPU_LOAD_AVG, MAX_CPU_LOAD_AVG_DEFAULT),
                        CommonPropertyUtils.getDouble(RESERVED_MEMORY, RESERVED_MEMORY_DEFAULT));

                if (!runCheckFlag) {
                    Thread.sleep(SLEEP_TIME_MILLIS);
                    continue;
                }

                register.blockUtilAcquireLock(CATALOG_TASK_LOCK_KEY);

                command = jobExternalService.getCatalogCommand();

                if (command != null) {

                    CatalogTask task = jobExternalService.executeCatalogCommand(command);
                    if (task != null) {
                        log.info("start submit catalog task : {} ", JSONUtils.toJsonString(task));

                        jobExternalService.deleteCommandById(command.getId());
                        log.info(String.format("submit success, catalog task : %s", task.getParameter()) );
                    }

                    register.release(CATALOG_TASK_LOCK_KEY);
                    ThreadUtils.sleep(SLEEP_TIME_MILLIS);
                } else {
                    register.release(CATALOG_TASK_LOCK_KEY);
                    ThreadUtils.sleep(SLEEP_TIME_MILLIS * 2);
                }

                retryNum = 0;
            } catch (Exception e){
                retryNum++;

                log.error("schedule catalog task error ", e);
                ThreadUtils.sleep(SLEEP_TIME_MILLIS * RETRY_BACKOFF [retryNum % RETRY_BACKOFF.length]);
            } finally {
                register.release(CATALOG_TASK_LOCK_KEY);
            }
        }
    }
}
