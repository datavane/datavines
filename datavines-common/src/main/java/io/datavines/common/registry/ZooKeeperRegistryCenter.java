package io.datavines.common.registry;

import java.util.Set;

import io.datavines.common.config.CoreConfig;
import io.datavines.common.zookeeper.ZooKeeperClient;

public class ZooKeeperRegistryCenter implements IRegistryCenter {

    private final ZooKeeperClient zooKeeperClient;

    public ZooKeeperRegistryCenter(){
        this.zooKeeperClient = ZooKeeperClient.getInstance();
    }

    @Override
    public Set<String> getAllMasterNodes() {
        return null;
    }

    @Override
    public String getActiveMaster(String path) {
        return zooKeeperClient.get(path);
    }

    @Override
    public String getActiveMaster() {
        return zooKeeperClient.get(CoreConfig.COORDINATOR_ACTIVE_DIR);
    }

}
