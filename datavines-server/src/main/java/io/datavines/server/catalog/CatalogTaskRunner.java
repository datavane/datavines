package io.datavines.server.catalog;

import io.datavines.server.catalog.task.*;
import io.datavines.server.utils.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class CatalogTaskRunner implements Runnable {

    private final CatalogTaskContext taskContext;

    private final CatalogTaskResponseQueue responseQueue =
            SpringApplicationContext.getBean(CatalogTaskResponseQueue.class);

    public CatalogTaskRunner(CatalogTaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void run() {
        CatalogMetaDataFetchTaskImpl fetchTask = new CatalogMetaDataFetchTaskImpl(taskContext.getMetaDataFetchRequest());
        try {
            fetchTask.execute();
            log.info("fetch metadata finished");
            responseQueue.add(new CatalogTaskResponse(taskContext.getCatalogTaskId(), 1));
        } catch (SQLException e) {
            log.error("fetch metadata error: ", e);
            responseQueue.add(new CatalogTaskResponse(taskContext.getCatalogTaskId(), 2));
        }
    }
}
