package io.datavines.storage.api;

import io.datavines.spi.SPI;

@SPI
public interface StorageFactory {

    StorageConnector getStorageConnector();
}
