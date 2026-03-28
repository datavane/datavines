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
package io.datavines.server.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.datavines.server.api.dto.bo.catalog.lineage.LineageEntityEdgeInfo;
import io.datavines.server.api.dto.bo.catalog.lineage.SqlWithDataSourceKeyProperties;
import io.datavines.server.api.dto.bo.catalog.lineage.SqlWithDataSourceList;
import io.datavines.server.api.dto.vo.catalog.lineage.CatalogEntityLineageVO;
import io.datavines.server.repository.entity.catalog.CatalogEntityRel;

public interface CatalogEntityRelService extends IService<CatalogEntityRel> {

    public boolean addLineage(LineageEntityEdgeInfo entityEdgeInfo);

    public boolean addLineageByParseSql(SqlWithDataSourceList sqlWithDataSourceList);

    public boolean addLineageByParseSql2(SqlWithDataSourceKeyProperties sqlWithDataSourceKeyProperties);

    public CatalogEntityLineageVO getLineageByFqn(Long datasourceId, String fqn, int upstreamDepth, int downstreamDepth);

    public CatalogEntityLineageVO getLineageByUUID(String uuid, int upstreamDepth, int downstreamDepth);

    public boolean deleteLineage(String fromUUID, String toUUID);
}
