package io.datavines.engine.spark.api.batch;

import io.datavines.engine.spark.api.BaseSparkSource;
import io.datavines.engine.spark.api.BaseSparkTransform;
import io.datavines.engine.spark.api.SparkRuntimeEnvironment;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

import io.datavines.common.config.Config;
import io.datavines.common.config.ConfigRuntimeException;
import io.datavines.engine.api.env.Execution;

import static io.datavines.engine.api.EngineConstants.*;

/**
 * SparkBatchExecution
 */
public class SparkBatchExecution implements Execution<SparkBatchSource, BaseSparkTransform, SparkBatchSink> {

    private final SparkRuntimeEnvironment environment;

    public SparkBatchExecution(SparkRuntimeEnvironment environment) throws ConfigRuntimeException {
        this.environment = environment;
    }

    @Override
    public void execute(List<SparkBatchSource> sources, List<BaseSparkTransform> transforms, List<SparkBatchSink> sinks) {
        sources.forEach(s -> {
                registerInputTempView(s, environment);
        });

        if (!sources.isEmpty()) {
            Dataset<Row> ds = sources.get(0).getData(environment);
            for (BaseSparkTransform tf:transforms) {
                ds = transformProcess(environment, tf, ds);
                registerTransformTempView(tf, ds);
            }

            for (SparkBatchSink sink: sinks) {
                sinkProcess(environment, sink, ds);
            }
        }
    }

    private void registerTempView(String tableName, Dataset<Row> ds) {
        ds.createOrReplaceTempView(tableName);
    }

    private void registerInputTempView(BaseSparkSource<Dataset<Row>> source, SparkRuntimeEnvironment environment) {
        Config conf = source.getConfig();
        if (conf.has(OUTPUT_TABLE)) {
            String tableName = conf.getString(OUTPUT_TABLE);
            registerTempView(tableName, source.getData(environment));
        } else {
            throw new ConfigRuntimeException(
                    "Plugin[" + source.getClass().getName() + "] must be registered as dataset/table, please set \"result_table_name\" config");
        }
    }

    private Dataset<Row> transformProcess(SparkRuntimeEnvironment environment, BaseSparkTransform transform, Dataset<Row> ds) {
        Config config = transform.getConfig();
        Dataset<Row> fromDs;
        Dataset<Row> resultDs = null;
        if (config.has(INPUT_TABLE)) {
            String[] tableNames = config.getString(INPUT_TABLE).split(",");

            for (String sourceTableName: tableNames) {
                fromDs = environment.sparkSession().read().table(sourceTableName);

                if(resultDs == null) {
                    resultDs = fromDs;
                } else {
                    resultDs = resultDs.union(fromDs);
                }
            }
        } else {
            resultDs = ds;
        }

        if (config.has(TMP_TABLE)) {
            if(resultDs == null) {
                resultDs = ds;
            }
            String tableName = config.getString(TMP_TABLE);
            registerTempView(tableName, resultDs);
        }

        return transform.process(resultDs, environment);
    }

    private void registerTransformTempView(BaseSparkTransform plugin, Dataset<Row> ds) {
        Config config = plugin.getConfig();
        if (config.has(OUTPUT_TABLE)) {
            String tableName = config.getString(OUTPUT_TABLE);
            registerTempView(tableName, ds);
        }
    }

    private void sinkProcess(SparkRuntimeEnvironment environment, SparkBatchSink sink, Dataset<Row> ds) {
        Config config = sink.getConfig();
        Dataset<Row> fromDs = ds;
        if (config.has(INPUT_TABLE)) {
            String sourceTableName = config.getString(INPUT_TABLE);
            fromDs = environment.sparkSession().read().table(sourceTableName);
        }
        sink.output(fromDs, environment);
    }

    @Override
    public void stop() {

    }
}
