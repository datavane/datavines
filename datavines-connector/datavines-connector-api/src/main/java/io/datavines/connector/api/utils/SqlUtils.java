/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.connector.api.utils;

import io.datavines.common.exception.DataVinesException;
import io.datavines.common.utils.StringUtils;
import io.datavines.connector.api.entity.QueryColumn;
import io.datavines.connector.api.entity.ResultList;
import io.datavines.connector.api.entity.ResultListWithColumns;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class SqlUtils {

    public static ResultListWithColumns getListWithHeaderFromResultSet(ResultSet rs, Set<String> queryFromsAndJoins) throws SQLException {

        ResultListWithColumns resultListWithColumns = new ResultListWithColumns();

        ResultSetMetaData metaData = rs.getMetaData();

        List<QueryColumn> queryColumns = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String key = getColumnLabel(queryFromsAndJoins, metaData.getColumnLabel(i));
            queryColumns.add(new QueryColumn(key, metaData.getColumnTypeName(i),""));
        }
        resultListWithColumns.setColumns(queryColumns);

        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            while (rs.next()) {
                resultList.add(getResultObjectMap(rs, metaData));
            }
        } catch (Throwable e) {
            log.error("get result set error: {0}", e);
        }

        resultListWithColumns.setResultList(resultList);
        return resultListWithColumns;
    }

    public static ResultListWithColumns getListWithHeaderFromResultSet(ResultSet rs, int start, int end) throws SQLException {

        ResultListWithColumns resultListWithColumns = new ResultListWithColumns();

        ResultSetMetaData metaData = rs.getMetaData();

        List<QueryColumn> queryColumns = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            queryColumns.add(new QueryColumn(metaData.getColumnLabel(i), metaData.getColumnTypeName(i),""));
        }
        resultListWithColumns.setColumns(queryColumns);
        resultListWithColumns.setResultList(getPage(rs, start, end, metaData));
        return resultListWithColumns;
    }

    public static ResultList getPageFromResultSet(ResultSet rs, int start, int end) throws SQLException {

        ResultList result = new ResultList();
        ResultSetMetaData metaData = rs.getMetaData();
        result.setResultList(getPage(rs, start, end, metaData));
        return result;
    }

    private static List<Map<String, Object>> getPage(ResultSet rs, int start, int end, ResultSetMetaData metaData) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            if (start > 0) {
                rs.absolute(start);
            }
            int current = start;
            while (rs.next()) {
                if (current >= start && current < end ){
                    resultList.add(getResultObjectMap(rs, metaData));
                    if (current == end-1){
                        break;
                    }
                    current++;
                } else {
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("get result set error: {0}", e);
        }

        return resultList;
    }

    public static ResultList getListFromResultSet(ResultSet rs) throws SQLException {

        ResultList result = new ResultList();
        ResultSetMetaData metaData = rs.getMetaData();

        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            while (rs.next()) {
                resultList.add(getResultObjectMap(rs, metaData));
            }
        } catch (Throwable e) {
            log.error("get result set error: {0}", e);
        }

        result.setResultList(resultList);
        return result;
    }

    private static Map<String, Object> getResultObjectMap(ResultSet rs, ResultSetMetaData metaData) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String key = metaData.getColumnLabel(i);
            Object value = rs.getObject(key);
            map.put(key.toLowerCase(), value instanceof byte[] ? new String((byte[]) value) : value);
        }

        return map;
    }

    private static String getColumnLabel(Set<String> columnPrefixes, String columnLabel) {
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

    public static void dropView(String viewName, Connection connection) {
        try (Statement statement = connection.createStatement()){
            if (!StringUtils.isEmptyOrNullStr(viewName)) {
                statement.execute("DROP VIEW " + viewName);
            }
        } catch (Exception e) {
            log.error("drop {} view error",viewName);
        }
    }

    public static void dropView(String viewName, Statement statement) {
        try {
            if (!StringUtils.isEmptyOrNullStr(viewName)) {
                statement.execute("DROP VIEW " + viewName);
            }
        } catch (Exception e) {
            log.error("drop {} view error", viewName);
        }
    }

    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception e) {
                log.error("close result set error : ", e);
            }
        }
    }

    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
                log.error("close statement error : ", e);
            }
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("close connection error : ", e);
            }
        }
    }

    public static List<String> extractTablesFromSelect(String sql) {
        List<String> tables;
        try {
            // 解析 SQL 查询语句
            tables = new ArrayList<>(TablesNamesFinder.findTables(sql));
        } catch (Exception e) {
            throw new DataVinesException("extract tables from select error", e);
        }
        return tables;
    }
}
