package io.datavines.engine.spark.jdbc.source;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.common.utils.TypesafeConfigUtils;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.spark.api.SparkRuntimeEnvironment;
import io.datavines.engine.spark.api.batch.SparkBatchSource;

/**
 * AbstractJdbcSource
 */
public abstract class BaseJdbcSource implements SparkBatchSource {

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
        List<String> requiredOptions = Arrays.asList("url", "table", "user", "password");

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x->{
            if(!config.has(x)){
                nonExistsOptions.add(x);
            }
        });

        if (!nonExistsOptions.isEmpty()) {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new CheckResult(true, "");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment prepareEnv) {

    }

    @Override
    public Dataset<Row> getData(SparkRuntimeEnvironment env) {
        return jdbcReader(env.sparkSession(), getDriver()).load();
    }

    private DataFrameReader jdbcReader(SparkSession sparkSession , String driver) {

        DataFrameReader reader = sparkSession.read()
                .format("jdbc")
                .option("url", config.getString("url"))
                .option("dbtable", config.getString("table"))
                .option("user", config.getString("user"))
                .option("password", config.getString("password"))
                .option("driver", driver);


        Config jdbcConfig = TypesafeConfigUtils.extractSubConfigThrowable(config, "jdbc.", false);

        if(!config.isEmpty()) {
            Map<String,String> optionMap = new HashMap<>(16);
            jdbcConfig.entrySet().forEach(x -> {
                optionMap.put(x.getKey(),String.valueOf(x.getValue()));
            });
            reader.options(optionMap);
        }

        return reader;
    }

    protected abstract String getDriver();
}
