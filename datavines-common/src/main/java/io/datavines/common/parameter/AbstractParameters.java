package io.datavines.common.parameter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractParameters implements IParameters {

    @Override
    public abstract boolean checkParameters();

    /**
     * local parameters
     */
    public List<Property> localParams;

    /**
     * get local parameters list
     * @return Property list
     */
    public List<Property> getLocalParams() {
        return localParams;
    }

    public void setLocalParams(List<Property> localParams) {
        this.localParams = localParams;
    }

    /**
     * get local parameters map
     * @return parameters map
     */
    public Map<String, Property> getLocalParametersMap() {
        if (localParams != null) {
            Map<String, Property> localParametersMaps = new LinkedHashMap<>();

            for (Property property : localParams) {
                localParametersMaps.put(property.getKey(),property);
            }
            return localParametersMaps;
        }
        return null;
    }
}
