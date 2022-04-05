package io.datavines.engine.jdbc.transform.sql;

import io.datavines.engine.jdbc.api.entity.ResultList;

import java.sql.Connection;

public interface ITransformExecutor {

    ResultList execute(Connection connection, String sql, String outputTable) throws Exception;
}
