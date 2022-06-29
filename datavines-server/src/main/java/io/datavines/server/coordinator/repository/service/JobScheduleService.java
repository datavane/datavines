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

package io.datavines.server.coordinator.repository.service;


import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleCreate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleCreateOrUpdate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.JobScheduleUpdate;
import io.datavines.server.coordinator.api.entity.dto.job.schedule.MapParam;
import io.datavines.server.coordinator.repository.entity.Job;
import io.datavines.server.coordinator.repository.entity.JobSchedule;


import java.util.List;

public interface JobScheduleService {

    JobSchedule create(JobScheduleCreate jobScheduleCreate) throws DataVinesServerException;

    JobSchedule update(JobScheduleUpdate jobScheduleUpdate) throws DataVinesServerException;

    int deleteById(long id);

    JobSchedule getById(long id);

    JobSchedule getByJobId(Long jobId);

    List<String> getCron(MapParam mapParam);

}
