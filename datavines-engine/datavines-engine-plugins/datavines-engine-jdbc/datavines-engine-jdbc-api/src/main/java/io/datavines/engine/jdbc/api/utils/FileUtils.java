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
package io.datavines.engine.jdbc.api.utils;

import io.datavines.engine.jdbc.api.entity.QueryColumn;
import io.datavines.engine.jdbc.api.entity.ResultListWithColumns;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class FileUtils {

    public static void writeToLocal(ResultListWithColumns resultListWithColumns, String directory, String name) {
        //首先判断文件夹是否存在

        BufferedWriter bw = null;
        try {

            File localErrorDir = new File(directory);

            if (!localErrorDir.exists()){
                org.apache.commons.io.FileUtils.forceMkdir(localErrorDir);
            }

            bw = new BufferedWriter(new FileWriter(directory + File.separator + name +".csv"));

            if (resultListWithColumns != null && CollectionUtils.isNotEmpty(resultListWithColumns.getResultList())) {
                List<QueryColumn> columns = resultListWithColumns.getColumns();
                List<String> headerList = new ArrayList<>();
                columns.forEach(header -> {
                    headerList.add(header.getName());
                });

                bw.write(String.join("\001",headerList));
                bw.newLine();

                for(Map<String, Object> row: resultListWithColumns.getResultList()) {
                    List<String> rowDataList = new ArrayList<>();
                    headerList.forEach(header -> {
                        rowDataList.add((String.valueOf(row.get(header))));
                    });
                    bw.write(String.join("\001",rowDataList));
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            }
        } catch (IOException e) {
            log.error("write data error {}", e);
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ioe) {
                log.error("close buffer writer error {}", ioe);
            }
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ioe) {
                log.error("close buffer writer error {}", ioe);
            }
        }
//        File errorDataFile = new File(directory + File.separator + name);




    }
}
