package io.datavines.common.entity.job.builder;

import com.fasterxml.jackson.databind.util.BeanUtil;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.entity.ConnectorParameter;
import io.datavines.common.entity.TaskParameter;
import io.datavines.common.entity.job.DataQualityJobParameter;
import io.datavines.common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataQualityTaskParameterBuilder implements ParameterBuilder {

    @Override
    public List<String> buildTaskParameter(String jobParameter, ConnectionInfo srcConnectionInfo, ConnectionInfo targetConnectionInfo) {
        List<DataQualityJobParameter> jobParameters = JSONUtils.toList(jobParameter, DataQualityJobParameter.class);

        if (CollectionUtils.isNotEmpty(jobParameters)) {
            List<String> taskParameters = new ArrayList<>();
            jobParameters.forEach(jobParam -> {
                TaskParameter taskParameter = new TaskParameter();
                taskParameter.setMetricType(jobParam.getMetricType());
                Map<String,Object> metricParameters = jobParam.getMetricParameter();
                String database = (String)metricParameters.get("database");
                metricParameters.remove("database");
                taskParameter.setMetricParameter(metricParameters);
                taskParameter.setExpectedType(jobParam.getExpectedType());
                taskParameter.setExpectedParameter(jobParam.getExpectedParameter());
                taskParameter.setResultFormula(jobParam.getResultFormula());
                taskParameter.setOperator(jobParam.getOperator());
                taskParameter.setThreshold(jobParam.getThreshold());

                ConnectorParameter srcConnectorParameter = new ConnectorParameter();
                srcConnectorParameter.setType(srcConnectionInfo.getType());
                Map<String,Object> srcConnectorParameterMap = srcConnectionInfo.configMap();
                srcConnectorParameterMap.put("database", database);
                srcConnectorParameter.setParameters(srcConnectorParameterMap);
                taskParameter.setSrcConnectorParameter(srcConnectorParameter);

                String taskParameterStr = JSONUtils.toJsonString(taskParameter);
                taskParameters.add(taskParameterStr);
            });

            return taskParameters;
        }

        return null;
    }
}
