package io.datavines.engine.spark.api;

import io.datavines.engine.api.component.Component;

/**
 * BaseSparkSource
 */
public interface BaseSparkSource<DATA> extends Component {

    DATA getData(SparkRuntimeEnvironment env);
}
