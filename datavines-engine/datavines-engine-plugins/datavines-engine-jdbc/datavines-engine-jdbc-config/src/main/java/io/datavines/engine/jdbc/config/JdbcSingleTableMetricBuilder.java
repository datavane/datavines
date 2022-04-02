package io.datavines.engine.jdbc.config;

import io.datavines.common.config.SinkConfig;
import io.datavines.common.config.enums.SinkType;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.datavines.engine.config.ConfigConstants.UNIQUE_CODE;
import static io.datavines.engine.config.MetricParserUtils.generateUniqueCode;

public class JdbcSingleTableMetricBuilder extends BaseJdbcConfigurationBuilder {

    @Override
    public void buildSinkConfigs() throws DataVinesException {

        inputParameter.put(UNIQUE_CODE, StringUtils.wrapperSingleQuotes(generateUniqueCode(inputParameter)));
        List<SinkConfig> sinkConfigs = new ArrayList<>();
        //get the actual value storage parameter
        SinkConfig actualValueSinkConfig = getDefaultSinkConfig(SinkSqlBuilder.getActualValueSql(),"actual_values");
        actualValueSinkConfig.setType(SinkType.ACTUAL_VALUE.getDescription());
        sinkConfigs.add(actualValueSinkConfig);

        //get the task data storage parameter
        SinkConfig taskResultSinkConfig = getDefaultSinkConfig(SinkSqlBuilder.getTaskResultSql(), "task_result");
        taskResultSinkConfig.setType(SinkType.TASK_RESULT.getDescription());
        sinkConfigs.add(taskResultSinkConfig);

        //get the error data storage parameter
        //support file(hdfs/minio/s3)/es

        configuration.setSinkParameters(sinkConfigs);
    }

}
