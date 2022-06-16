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
package io.datavines.notification.api.spi;

import io.datavines.notification.api.entity.SlasNotificationMessage;
import io.datavines.notification.api.entity.SlasNotificationResult;
import io.datavines.notification.api.entity.SlasReceiverMessage;
import io.datavines.notification.api.entity.SlasSenderMessage;
import io.datavines.spi.SPI;

import java.util.Map;
import java.util.Set;


@SPI
public interface SlasHandlerPlugin {
    /**
     * save message to db then send message to receiver , return the status finally
     * @param slasNotificationMessage issue message a
     * @return send status
     */
    SlasNotificationResult notify(SlasNotificationMessage slasNotificationMessage, Map<SlasSenderMessage, Set<SlasReceiverMessage>> config);

    String getConfigSenderJson();

    String getConfigJson();

    String getConfigReceiverJson();
}
