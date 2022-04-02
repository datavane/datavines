package io.datavines.engine.spark.api;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.StreamingContext;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.engine.api.env.Execution;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.spark.api.batch.SparkBatchExecution;

/**
 * SparkRuntimeEnvironment
 */
public class SparkRuntimeEnvironment implements RuntimeEnvironment {

    private static final String TYPE = "type";
    private static final String STREAM = "stream";
    private static final String BATCH = "batch";

    private SparkSession sparkSession;

    private StreamingContext streamingContext;

    private Config config = new Config();

    @Override
    public void setConfig(Config config) {
        if(config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    @Override
    public CheckResult checkConfig() {
        return new CheckResult(true, "");
    }

    @Override
    public void prepare() {
        sparkSession = SparkSession.builder().config(createSparkConf()).getOrCreate();
        this.createStreamingContext();
    }

    private SparkConf createSparkConf() {
        SparkConf conf = new SparkConf();
        this.config.entrySet()
                .forEach(entry -> {
                    conf.set(entry.getKey(), String.valueOf(entry.getValue()));
        });

        return conf;
    }

    private void createStreamingContext() {
        SparkConf conf = sparkSession.sparkContext().getConf();
        long duration = conf.getLong("spark.stream.batchDuration", 5);
        if (streamingContext == null) {
            streamingContext =
                    new StreamingContext(sparkSession.sparkContext(), Seconds.apply(duration));
        }
    }

    public SparkSession sparkSession() {
        return sparkSession;
    }

    public StreamingContext streamingContext() {
        return streamingContext;
    }

    @Override
    public Execution getExecution() {
        Execution execution = null;
        if (BATCH.equals(config.getString(TYPE).toLowerCase())) {
            execution = new SparkBatchExecution(this);
        }
        return execution;
    }
}
