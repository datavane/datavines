package io.datavines.server.catalog;

import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.common.utils.Stopper;
import io.datavines.common.utils.ThreadUtils;
import io.datavines.server.catalog.task.CatalogTaskContext;
import io.datavines.server.catalog.task.CatalogTaskResponse;
import io.datavines.server.catalog.task.CatalogTaskResponseQueue;
import io.datavines.server.catalog.task.MetaDataFetchRequest;
import io.datavines.server.repository.entity.catalog.CatalogTask;
import io.datavines.server.repository.service.CatalogTaskService;
import io.datavines.server.utils.NamedThreadFactory;
import io.datavines.server.utils.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class CatalogTaskManager {

    private final LinkedBlockingQueue<CatalogTaskContext> taskQueue = new LinkedBlockingQueue<>();

    private final CatalogTaskResponseQueue responseQueue =
            SpringApplicationContext.getBean(CatalogTaskResponseQueue.class);

    private final CatalogTaskService catalogTaskService =
            SpringApplicationContext.getBean(CatalogTaskService.class);

    private final ExecutorService taskExecuteService;

    public CatalogTaskManager () {
        this.taskExecuteService = Executors.newFixedThreadPool(
                CommonPropertyUtils.getInt(CommonPropertyUtils.EXEC_THREADS,CommonPropertyUtils.EXEC_THREADS_DEFAULT),
                new NamedThreadFactory("CatalogTask-Execute-Thread"));
    }

    public void init() {
        new TaskExecutor().start();

        new TaskResponseOperator().start();
    }

    class TaskExecutor extends Thread {

        @Override
        public void run() {
            while(Stopper.isRunning()) {
                try {
                    CatalogTaskContext catalogTaskContext = taskQueue.take();
                    taskExecuteService.execute(new CatalogTaskRunner(catalogTaskContext));
                } catch(Exception e) {
                    log.error("dispatcher catalog task error",e);
                    ThreadUtils.sleep(2000);
                }
            }
        }
    }

    /**
     * operate task response
     */
    class TaskResponseOperator extends Thread {

        @Override
        public void run() {
            while (Stopper.isRunning()) {
                try {
                    CatalogTaskResponse taskResponse = responseQueue.take();
                    CatalogTask catalogTask = catalogTaskService.getById(taskResponse.getCatalogTaskId());
                    if (catalogTask != null) {
                        catalogTask.setStatus(taskResponse.getStatus());
                        catalogTaskService.update(catalogTask);
                    }

                } catch(Exception e) {
                    log.info("operate catalog task response error {0}", e);
                }
            }
        }
    }

}
