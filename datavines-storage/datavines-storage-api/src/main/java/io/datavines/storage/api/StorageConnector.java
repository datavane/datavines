package io.datavines.storage.api;

import java.util.Map;

public interface StorageConnector {

    String getConfigJson();

    Map<String,Object> getParamMap(Map<String, Object> parameter);
}
