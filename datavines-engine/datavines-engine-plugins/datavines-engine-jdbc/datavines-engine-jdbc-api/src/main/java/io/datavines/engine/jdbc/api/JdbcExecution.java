package io.datavines.engine.jdbc.api;

import io.datavines.common.config.enums.SinkType;
import io.datavines.common.config.enums.SourceType;
import io.datavines.common.config.enums.TransformType;
import io.datavines.engine.api.env.Execution;
import io.datavines.engine.jdbc.api.entity.ResultList;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcExecution implements Execution<JdbcSource,JdbcTransform,JdbcSink> {

    private final JdbcRuntimeEnvironment jdbcRuntimeEnvironment;

    public JdbcExecution(JdbcRuntimeEnvironment jdbcRuntimeEnvironment){
        this.jdbcRuntimeEnvironment = jdbcRuntimeEnvironment;
    }

    @Override
    public void execute(List<JdbcSource> sources, List<JdbcTransform> transforms, List<JdbcSink> sinks) throws SQLException {
        if(CollectionUtils.isEmpty(sources)) {
            return;
        }

        sources.forEach(jdbcSource -> {
            switch (SourceType.of(jdbcSource.getConfig().getString("plugin_type"))){
                case NORMAL:
                    jdbcRuntimeEnvironment.setSourceConnection(jdbcSource.getConnection(jdbcRuntimeEnvironment));
                    break;
                case METADATA:
                    jdbcRuntimeEnvironment.setMetadataConnection(jdbcSource.getConnection(jdbcRuntimeEnvironment));
                    break;
                default:
                    break;
            }
        });

        List<ResultList> taskResult = new ArrayList<>();
        List<ResultList> actualValue = new ArrayList<>();
        transforms.forEach(jdbcTransform -> {
            switch (TransformType.of(jdbcTransform.getConfig().getString("plugin_type"))){
                case INVALIDATE_ITEMS:
                    jdbcTransform.process(jdbcRuntimeEnvironment);
                    break;
                case ACTUAL_VALUE:
                    ResultList actualValueResult = jdbcTransform.process(jdbcRuntimeEnvironment);
                    actualValue.add(actualValueResult);
                    taskResult.add(actualValueResult);
                    break;
                case EXPECTED_VALUE_FROM_DEFAULT_SOURCE:
                case EXPECTED_VALUE_FROM_SRC_SOURCE:
                    ResultList expectedResult = jdbcTransform.process(jdbcRuntimeEnvironment);
                    taskResult.add(expectedResult);
                default:
                    break;
            }
        });

        sinks.forEach(jdbcSink -> {
            switch (SinkType.of(jdbcSink.getConfig().getString("plugin_type"))){
                case ACTUAL_VALUE:
                    jdbcSink.output(actualValue, jdbcRuntimeEnvironment);
                    break;
                case TASK_RESULT:
                    jdbcSink.output(taskResult, jdbcRuntimeEnvironment);
                default:
                    break;
            }
        });

        jdbcRuntimeEnvironment.close();
    }

    @Override
    public void stop() {

    }
}
