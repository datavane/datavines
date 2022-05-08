package io.datavines.registry.api;

public final class RegistryException extends RuntimeException {

    public RegistryException(String msg) {
        super(msg);
    }

    public RegistryException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
