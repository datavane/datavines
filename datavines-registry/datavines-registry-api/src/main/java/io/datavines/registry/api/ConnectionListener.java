package io.datavines.registry.api;

public interface ConnectionListener {
    void onUpdate(ConnectionStatus status);
}
