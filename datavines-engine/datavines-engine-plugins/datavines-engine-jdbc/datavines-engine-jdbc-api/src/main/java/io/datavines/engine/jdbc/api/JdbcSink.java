package io.datavines.engine.jdbc.api;

import io.datavines.engine.api.component.Component;
import io.datavines.engine.jdbc.api.entity.ResultList;

import java.util.List;

public interface JdbcSink extends Component {

    void output(List<ResultList> resultList, JdbcRuntimeEnvironment env);
}
