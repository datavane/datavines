package io.datavines.common.parameter;

import java.util.List;

/**
 * 
 */
public interface IParameters {

    /**
     * check parameters
     * @return
     */
    boolean checkParameters();

    /**
     * get resource files
     * @return
     */
    List<ResourceInfo> getResourceFiles();
}
