package io.datavines.common.registry;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class LocalRegistryCenter implements IRegistryCenter {

    @Override
    public Set<String> getAllMasterNodes() {
        Set<String> nodes = new HashSet<>();
        nodes.add("127.0.0.1:8899");
        return nodes;
    }

    @Override
    public String getActiveMaster(String path) {
        return null;
    }

    @Override
    public String getActiveMaster() {
        return null;
    }

}
