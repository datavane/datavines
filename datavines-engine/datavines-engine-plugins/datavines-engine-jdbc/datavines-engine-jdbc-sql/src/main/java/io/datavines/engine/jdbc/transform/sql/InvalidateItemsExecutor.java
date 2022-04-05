package io.datavines.engine.jdbc.transform.sql;

import io.datavines.engine.jdbc.api.entity.ResultList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class InvalidateItemsExecutor implements ITransformExecutor {

    @Override
    public ResultList execute(Connection connection, String sql, String outputTable) throws Exception {
        Statement statement = connection.createStatement();

        statement.execute("drop view if exists " + outputTable);
        statement.execute("create view " + outputTable + " as " + sql);
        ResultSet resultSet = statement.executeQuery("select * from " + outputTable);
        ResultList resultList = SqlUtils.getListFromResultSet(resultSet, SqlUtils.getQueryFromsAndJoins(sql));
        statement.close();
        resultSet.close();
        return resultList;
    }
}
