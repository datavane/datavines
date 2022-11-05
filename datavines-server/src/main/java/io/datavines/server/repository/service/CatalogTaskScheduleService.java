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


import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.api.dto.bo.catalog.schedule.CatalogTaskScheduleCreateOrUpdate;
import io.datavines.server.api.dto.bo.job.schedule.JobScheduleCreateOrUpdate;
import io.datavines.server.api.dto.bo.job.schedule.MapParam;
import io.datavines.server.repository.entity.JobSchedule;
import io.datavines.server.repository.entity.catalog.CatalogTaskSchedule;

import java.util.List;

public interface CatalogTaskScheduleService {

    CatalogTaskSchedule createOrUpdate(CatalogTaskScheduleCreateOrUpdate scheduleCreate) throws DataVinesServerException;

    int deleteById(long id);

    CatalogTaskSchedule getById(long id);

    CatalogTaskSchedule getByDataSourceId(Long dataSourceId);

    List<String> getCron(MapParam mapParam);

}
