package io.datavines.common.registry;

import java.util.Set;

/**
 * 
 */
public interface IRegistryCenter {

    /**
     * 获取所有的Master Node节点
     * @return
     */
    Set<String> getAllMasterNodes();

    String getActiveMaster(String path);

    String getActiveMaster();

}
