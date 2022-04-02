package io.datavines.engine.spark.transform.sql;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.spark.api.BaseSparkTransform;
import io.datavines.engine.spark.api.SparkRuntimeEnvironment;

/**
 * SqlTransform
 */
public class SqlTransform implements BaseSparkTransform {

    private Config config = new Config();

    @Override
    public void setConfig(Config config) {
        if(config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {
        if (config.has("sql")) {
            return new CheckResult(true, "");
        } else {
            return new CheckResult(false, "please specify [sql]");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment prepareEnv) {

    }

    @Override
    public Dataset<Row> process(Dataset<Row> data, SparkRuntimeEnvironment env) {
        return env.sparkSession().sql(config.getString("sql"));
    }
}
