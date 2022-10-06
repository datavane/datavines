package io.datavines.server.catalog.task;

import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;

@Component
public class CatalogTaskResponseQueue {

    private final LinkedBlockingQueue<CatalogTaskResponse> responseQueue = new LinkedBlockingQueue<>();

    public boolean add(CatalogTaskResponse catalogTaskResponse) {
        return responseQueue.add(catalogTaskResponse);
    }

    public CatalogTaskResponse take() throws InterruptedException {
        return responseQueue.take();
    }
}
