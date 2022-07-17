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
package io.datavines.storage.plugin;

import io.datavines.common.entity.ListWithQueryColumn;
import io.datavines.common.entity.QueryColumn;
import io.datavines.common.param.ConnectorResponse;
import io.datavines.common.param.ExecuteRequestParam;
import io.datavines.common.utils.JSONUtils;
import io.datavines.storage.api.StorageExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class LocalFileStorageExecutor implements StorageExecutor {

    @Override
    public ConnectorResponse executeSyncQuery(ExecuteRequestParam param) throws Exception {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();

        int pageNumber = param.getPageNumber();
        int pageSize  = param.getPageSize();

        Map<String,String> parameterMap = JSONUtils.toMap(param.getDataSourceParam(), String.class, String.class);
        String dir = parameterMap.get("error_data_file_dir");
        String filePath = dir +"/" + param.getScript() + ".csv";

        if (pageNumber < 1) {
            pageNumber = 1;
        }

        if (pageSize < 1) {
            pageSize = 10;
        }

        builder.result(readForPage(filePath, pageNumber, pageSize));

        return builder.build();
    }

    private List<String> readPartFileContent(String filePath,
                                             int skipLine,
                                             int limit){
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("read file error",e);
        }
        return Collections.emptyList();
    }

    private ListWithQueryColumn readForPage(String filePath, int pageNumber, int pageSize) throws Exception{
        int startRow = (pageNumber - 1) * pageSize + 1;
        ListWithQueryColumn listWithQueryColumn = new ListWithQueryColumn();
        List<String> headerList = readPartFileContent(filePath,0,1);
        if (CollectionUtils.isNotEmpty(headerList)) {
            String header = headerList.get(0);
            String[] headerTypeList = header.split("\001");
            Map<Integer, String> keyMap = new HashMap<>();
            List<QueryColumn> queryColumns = new ArrayList<>();
            for (int i = 0; i < headerTypeList.length; i++) {
                String[] columnSplit = headerTypeList[i].split("@@");
                keyMap.put(i, columnSplit[0]);
                queryColumns.add(new QueryColumn(columnSplit[0], columnSplit[1]));
            }
            listWithQueryColumn.setColumns(queryColumns);

            List<Map<String, Object>> resultList = new ArrayList<>();
            List<String> rowList = null;
            rowList = readPartFileContent(filePath,startRow,pageSize);
            if (CollectionUtils.isNotEmpty(rowList)) {
                for (String row: rowList) {
                    String[] rowDataList = row.split("\001");
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    for (int i=0; i<rowDataList.length; i++) {
                        rowMap.put(keyMap.get(i),rowDataList[i]);
                    }
                    resultList.add(rowMap);
                }
            }

            listWithQueryColumn.setResultList(resultList);
            listWithQueryColumn.setPageNumber(pageNumber);
            listWithQueryColumn.setPageSize(pageSize);

            Path path = Paths.get(filePath);
            listWithQueryColumn.setTotalCount(Files.lines(path).count());
        }

        return listWithQueryColumn;
    }
}
