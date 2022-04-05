package io.datavines.engine.jdbc.transform.sql;

import io.datavines.engine.jdbc.api.entity.ResultList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ExpectedValueExecutor implements ITransformExecutor {

    @Override
    public ResultList execute(Connection connection, String sql, String outputTable) throws Exception {

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        ResultList resultList = SqlUtils.getListFromResultSet(resultSet, SqlUtils.getQueryFromsAndJoins(sql));
        statement.close();
        resultSet.close();
        return resultList;
    }
}
