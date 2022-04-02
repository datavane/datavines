package io.datavines.engine.jdbc.api;

import io.datavines.engine.api.component.Component;
import io.datavines.engine.jdbc.api.entity.ResultList;

public interface JdbcTransform extends Component {

    ResultList process(JdbcRuntimeEnvironment env);
}
