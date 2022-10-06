package io.datavines.server.catalog.task;

import lombok.Data;

@Data
public class CatalogTaskContext {

    private MetaDataFetchRequest metaDataFetchRequest;

    private Long catalogTaskId;

    public CatalogTaskContext(MetaDataFetchRequest metaDataFetchRequest, Long catalogTaskId) {
        this.metaDataFetchRequest = metaDataFetchRequest;
        this.catalogTaskId = catalogTaskId;
    }
}
