package io.datavines.engine.config;

import io.datavines.common.config.DataVinesQualityConfig;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.entity.TaskInfo;
import io.datavines.common.entity.TaskParameter;

import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;

import java.util.Map;

public class DataVinesConfigurationManager {

    private static DataVinesQualityConfig buildConfiguration(DataQualityConfigurationBuilder builder) throws DataVinesException {
        builder.buildName();
        builder.buildEnvConfig();
        builder.buildSourceConfigs();
        builder.buildTransformConfigs();
        builder.buildSinkConfigs();
        return builder.build();
    }

    public static DataVinesQualityConfig generateConfiguration(
            Map<String, String> inputParameter,
            TaskInfo taskInfo,
            ConnectionInfo connectionInfo) throws DataVinesException {

        if(taskInfo == null){
            throw new DataVinesException("taskInfo can not be null");
        }

        if(taskInfo.getTaskParameter() == null){
            throw new DataVinesException("task parameter can not be null");
        }

        TaskParameter taskParameter = taskInfo.getTaskParameter();

        String metricType = taskParameter.getMetricType();
        if (StringUtils.isEmpty(metricType)) {
            throw new DataVinesException("metric type can not be null");
        }

        SqlMetric sqlMetric = PluginLoader
                .getPluginLoader(SqlMetric.class)
                .getNewPlugin(metricType);

        if (sqlMetric == null) {
            throw new DataVinesException("can not find the metric: " + metricType);
        }

        DataQualityConfigurationBuilder builder = PluginLoader
                .getPluginLoader(DataQualityConfigurationBuilder.class)
                .getOrCreatePlugin(taskInfo.getEngineType() + "_" + sqlMetric.getType().getDescription());
        builder.init(inputParameter, taskInfo, connectionInfo);

        return buildConfiguration(builder);
    }

}
