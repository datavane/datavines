package io.datavines.engine.spark.api;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.datavines.engine.api.component.Component;

/**
 * BaseSparkSink
 */
public interface BaseSparkSink<OUT> extends Component {

    /**
     * output
     */
    OUT output(Dataset<Row> data, SparkRuntimeEnvironment environment);
}
