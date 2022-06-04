package io.datavines.common.entity.job.builder;

import io.datavines.common.entity.ConnectionInfo;

import java.util.List;

public interface ParameterBuilder {

    List<String> buildTaskParameter(String jobParameter, ConnectionInfo srcConnectionInfo, ConnectionInfo targetConnectionInfo);
}
