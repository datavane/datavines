package io.datavines.server.registry;

import io.datavines.common.utils.Stopper;
import io.datavines.common.utils.ThreadUtils;
import io.datavines.registry.api.Registry;
import org.springframework.stereotype.Component;

@Component
public class RegistryHolder {

    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void blockUtilAcquireLock(String key) {
        while (Stopper.isRunning() && registry!= null &&!registry.acquire(key, 10)) {
            ThreadUtils.sleep(1000);
        }
    }

    public void release(String key) {
        if (registry != null) {
            registry.release(key);
        }
    }
}
