package io.datavines.server.catalog.task;

import lombok.Data;

@Data
public class CatalogTaskResponse {

    private Long catalogTaskId;

    private int status;

    public CatalogTaskResponse(Long catalogTaskId, int status) {
        this.catalogTaskId = catalogTaskId;
        this.status = status;
    }
}
