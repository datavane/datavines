package io.datavines.engine.jdbc.api;

import io.datavines.engine.api.component.Component;

import java.sql.Connection;

public interface JdbcSource extends Component {

    Connection getConnection(JdbcRuntimeEnvironment env);
}
