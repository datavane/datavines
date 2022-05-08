package io.datavines.registry.plugin;


import io.datavines.registry.api.ConnectionListener;
import io.datavines.registry.api.ConnectionStatus;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperConnectionStateListener implements ConnectionStateListener {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperConnectionStateListener.class);

    private final ConnectionListener listener;

    public ZookeeperConnectionStateListener(ConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {

        switch (newState) {
            case LOST:
                logger.warn("Registry disconnected");
                listener.onUpdate(ConnectionStatus.DISCONNECTED);
                break;
            case RECONNECTED:
                logger.warn("Registry reconnected");
                listener.onUpdate(ConnectionStatus.RECONNECTED);
                break;
            default:
                break;
        }
    }
}
