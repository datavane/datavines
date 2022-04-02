package io.datavines.engine.spark.api.batch;

import io.datavines.engine.spark.api.BaseSparkSource;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * SparkBatchSource
 */
public interface SparkBatchSource extends BaseSparkSource<Dataset<Row>> {
}
