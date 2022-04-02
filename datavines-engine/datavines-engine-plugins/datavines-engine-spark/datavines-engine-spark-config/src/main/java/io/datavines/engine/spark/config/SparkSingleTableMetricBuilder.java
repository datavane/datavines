package io.datavines.engine.spark.config;

import io.datavines.common.config.SinkConfig;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.metric.api.MetricConstants;
import java.util.ArrayList;
import java.util.List;

import static io.datavines.engine.config.ConfigConstants.UNIQUE_CODE;
import static io.datavines.engine.config.MetricParserUtils.generateUniqueCode;

public class SparkSingleTableMetricBuilder extends BaseSparkConfigurationBuilder {

    @Override
    public void buildSinkConfigs() throws DataVinesException {

        inputParameter.put(UNIQUE_CODE, StringUtils.wrapperSingleQuotes(generateUniqueCode(inputParameter)));
        List<SinkConfig> sinkConfigs = new ArrayList<>();
        //get the actual value storage parameter
        SinkConfig actualValueSinkConfig = getDefaultSinkConfig(MetricConstants.TASK_ACTUAL_VALUE_SINK_SQL,"actual_values");
        sinkConfigs.add(actualValueSinkConfig);

        String taskSinkSql = MetricConstants.DEFAULT_SINK_SQL;
        if (StringUtils.isEmpty(expectedValue.getOutputTable())) {
            taskSinkSql = taskSinkSql.replaceAll("full join \\$\\{expected_table}","");
        }

        //get the task data storage parameter
        SinkConfig taskResultSinkConfig = getDefaultSinkConfig(taskSinkSql, "task_result");
        sinkConfigs.add(taskResultSinkConfig);

        //get the error data storage parameter
        //support file(hdfs/minio/s3)/es

        configuration.setSinkParameters(sinkConfigs);
    }

}
