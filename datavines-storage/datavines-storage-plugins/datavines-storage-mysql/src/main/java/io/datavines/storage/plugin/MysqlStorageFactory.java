package io.datavines.storage.plugin;

import io.datavines.storage.api.StorageConnector;
import io.datavines.storage.api.StorageFactory;

public class MysqlStorageFactory implements StorageFactory {

    @Override
    public StorageConnector getStorageConnector() {
        return new MysqlStorageConnector();
    }
}
