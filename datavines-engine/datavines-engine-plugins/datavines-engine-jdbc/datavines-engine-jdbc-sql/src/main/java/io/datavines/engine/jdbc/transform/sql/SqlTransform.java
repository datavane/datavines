package io.datavines.engine.jdbc.transform.sql;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.common.config.enums.TransformType;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.jdbc.api.JdbcRuntimeEnvironment;
import io.datavines.engine.jdbc.api.JdbcTransform;
import io.datavines.engine.jdbc.api.entity.ResultList;
import io.datavines.engine.jdbc.api.entity.ResultListWithColumns;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static io.datavines.common.CommonConstants.DOT;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class SqlTransform implements JdbcTransform {

    private Config config = new Config();

    @Override
    public void setConfig(Config config) {
        if(config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {
        List<String> requiredOptions = Arrays.asList("sql", "plugin_type");

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x->{
            if(!config.has(x)){
                nonExistsOptions.add(x);
            }
        });

        if (!nonExistsOptions.isEmpty()) {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new CheckResult(true, "");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment env) {

    }

    @Override
    public ResultList process(JdbcRuntimeEnvironment env) {

        ResultList resultList = null;
        try {
            Statement statement = null;
            Set<String> queryFromsAndJoins = null;
            String outputTable = config.getString("invalidate_items_table");
            switch (TransformType.of(config.getString("plugin_type"))){
                case INVALIDATE_ITEMS:
                    statement = env.getSourceConnection().createStatement();

                    statement.execute("drop view if exists " + outputTable);
                    statement.execute("create view "
                            + outputTable
                            + " AS " + config.getString("sql"));
                    queryFromsAndJoins = getQueryFromsAndJoins(config.getString("sql"));
                    ResultSet invalidateItemsResultSet = statement.executeQuery("select * from " + outputTable);
                    resultList = getListFromResultSet(invalidateItemsResultSet, queryFromsAndJoins);
                    break;
                case ACTUAL_VALUE:
                    statement = env.getSourceConnection().createStatement();
                    ResultSet actualValueResultSet = statement.executeQuery(config.getString("sql"));
                    resultList = getListFromResultSet(actualValueResultSet, queryFromsAndJoins);
                    statement.execute("drop view " + outputTable);
                    break;
                case EXPECTED_VALUE_FROM_DEFAULT_SOURCE:
                    statement = env.getMetadataConnection().createStatement();
                    ResultSet expectedValueResult = statement.executeQuery(config.getString("sql"));
                    resultList = getListFromResultSet(expectedValueResult, queryFromsAndJoins);
                    break;
                case EXPECTED_VALUE_FROM_SRC_SOURCE:
                    statement = env.getSourceConnection().createStatement();
                    ResultSet expectedValueResultFromSrc = statement.executeQuery(config.getString("sql"));
                    resultList = getListFromResultSet(expectedValueResultFromSrc, queryFromsAndJoins);
                    break;
                default:
                    break;
            }

            if (statement != null) {
                statement.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    private ResultList getListFromResultSet(ResultSet rs, Set<String> queryFromsAndJoins) throws SQLException {

        ResultList result = new ResultListWithColumns();
        ResultSetMetaData metaData = rs.getMetaData();

        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            while (rs.next()) {
                resultList.add(getResultObjectMap(rs, metaData, queryFromsAndJoins));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        result.setResultList(resultList);

        rs.close();
        return result;
    }

    private static Map<String, Object> getResultObjectMap(ResultSet rs, ResultSetMetaData metaData, Set<String> queryFromsAndJoins) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String key = metaData.getColumnLabel(i);
            String label = getColumnLabel(queryFromsAndJoins, key);

            Object value = rs.getObject(key);
            map.put(label, value instanceof byte[] ? new String((byte[]) value) : value);
        }

        return map;
    }

    public static String getColumnLabel(Set<String> columnPrefixes, String columnLabel) {
        if (!CollectionUtils.isEmpty(columnPrefixes)) {
            for (String prefix : columnPrefixes) {
                if (columnLabel.startsWith(prefix)) {
                    return columnLabel.replaceFirst(prefix, EMPTY);
                }
                if (columnLabel.startsWith(prefix.toLowerCase())) {
                    return columnLabel.replaceFirst(prefix.toLowerCase(), EMPTY);
                }
                if (columnLabel.startsWith(prefix.toUpperCase())) {
                    return columnLabel.replaceFirst(prefix.toUpperCase(), EMPTY);
                }
            }
        }

        return columnLabel;
    }

    public static Set<String> getQueryFromsAndJoins(String sql) {
        Set<String> columnPrefixes = new HashSet<>();
        try {
            net.sf.jsqlparser.statement.Statement parse = CCJSqlParserUtil.parse(sql);
            Select select = (Select) parse;
            SelectBody selectBody = select.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                PlainSelect plainSelect = (PlainSelect) selectBody;
                columnPrefixExtractor(columnPrefixes, plainSelect);
            }

            if (selectBody instanceof SetOperationList) {
                SetOperationList setOperationList = (SetOperationList) selectBody;
                List<SelectBody> selects = setOperationList.getSelects();
                for (SelectBody optSelectBody : selects) {
                    PlainSelect plainSelect = (PlainSelect) optSelectBody;
                    columnPrefixExtractor(columnPrefixes, plainSelect);
                }
            }

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        return columnPrefixes;
    }

    private static void columnPrefixExtractor(Set<String> columnPrefixes, PlainSelect plainSelect) {
        getFromItemName(columnPrefixes, plainSelect.getFromItem());
        List<Join> joins = plainSelect.getJoins();
        if (!CollectionUtils.isEmpty(joins)) {
            joins.forEach(join -> getFromItemName(columnPrefixes, join.getRightItem()));
        }
    }

    private static void getFromItemName(Set<String> columnPrefixes, FromItem fromItem) {
        if (fromItem == null) {
            return;
        }
        Alias alias = fromItem.getAlias();
        if (alias != null) {
            if (alias.isUseAs()) {
                columnPrefixes.add(alias.getName().trim() + DOT);
            } else {
                columnPrefixes.add(alias.toString().trim() + DOT);
            }
        } else {
            fromItem.accept(getFromItemTableName(columnPrefixes));
        }
    }

    private static FromItemVisitor getFromItemTableName(Set<String> set) {
        return new FromItemVisitor() {
            @Override
            public void visit(Table tableName) {
                set.add(tableName.getName() + DOT);
            }

            @Override
            public void visit(SubSelect subSelect) {
            }

            @Override
            public void visit(SubJoin subjoin) {
            }

            @Override
            public void visit(LateralSubSelect lateralSubSelect) {
            }

            @Override
            public void visit(ValuesList valuesList) {
            }

            @Override
            public void visit(TableFunction tableFunction) {
            }

            @Override
            public void visit(ParenthesisFromItem aThis) {
            }
        };
    }
}
