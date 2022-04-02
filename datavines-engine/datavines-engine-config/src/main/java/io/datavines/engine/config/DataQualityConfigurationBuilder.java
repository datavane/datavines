package io.datavines.engine.config;

import io.datavines.common.config.DataVinesQualityConfig;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.entity.TaskInfo;
import io.datavines.common.exception.DataVinesException;
import io.datavines.spi.SPI;

import java.util.Map;

@SPI
public interface DataQualityConfigurationBuilder {

    void init(Map<String, String> inputParameter, TaskInfo taskInfo, ConnectionInfo connectionInfo);

    void buildName();

    void buildEnvConfig();

    void buildSourceConfigs() throws DataVinesException;

    void buildTransformConfigs();

    void buildSinkConfigs() throws DataVinesException;

    DataVinesQualityConfig build();
}
