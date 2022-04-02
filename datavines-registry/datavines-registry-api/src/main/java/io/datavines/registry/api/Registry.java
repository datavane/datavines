package io.datavines.registry.api;

import io.datavines.spi.SPI;

import java.util.List;
import java.util.Properties;

@SPI
public interface Registry {

    void init(Properties properties) throws Exception;

    boolean acquire(String key, long timeout);

    boolean release(String key);

    void subscribe(String key, SubscribeListener listener);

    void unSubscribe(String key);

    void addConnectionListener(ConnectionListener connectionListener);

    List<ServerInfo> getActiveServerList();
}
