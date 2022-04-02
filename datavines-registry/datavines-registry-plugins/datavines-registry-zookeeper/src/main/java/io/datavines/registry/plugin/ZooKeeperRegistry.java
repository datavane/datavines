package io.datavines.registry.plugin;

import io.datavines.common.zookeeper.ZooKeeperClient;
import io.datavines.registry.api.ConnectionListener;
import io.datavines.registry.api.Registry;
import io.datavines.registry.api.ServerInfo;
import io.datavines.registry.api.SubscribeListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ZooKeeperRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRegistry.class);

    private InterProcessMutex mutex;

    private CuratorFramework client;

    @Override
    public void init(Properties properties) {
        client = ZooKeeperClient.getInstance().getClient();
    }

    @Override
    public boolean acquire(String key, long timeout){

        if (mutex == null) {
            mutex = new InterProcessMutex(client,key);
        }

        try {
            return mutex.acquire(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public boolean release(String key){

        try {
            if (mutex != null && mutex.isAcquiredInThisProcess()) {
                mutex.release();
                return true;
            }
            return true;
        } catch (Exception e) {
            logger.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public void subscribe(String key, SubscribeListener listener) {

    }

    @Override
    public void unSubscribe(String key) {

    }

    @Override
    public void addConnectionListener(ConnectionListener connectionListener) {

    }

    @Override
    public List<ServerInfo> getActiveServerList() {
        return null;
    }
}
