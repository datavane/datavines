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

package io.datavines.server.command;

public enum CommandCode {
    /**
     * 默认
     */
    DEFAULT,
    /**
     * execute job request
     */
    TASK_EXECUTE_REQUEST,
    /**
     * execute job request
     */
    TASK_SUBMIT_REQUEST,
    /**
     * execute job ack
     */
    TASK_EXECUTE_ACK,
    /**
     * execute job ack
     */
    TASK_SUBMIT_ACK,
    /**
     * execute job response
     */
    TASK_EXECUTE_RESPONSE,
    /**
     * kill job request
     */
    TASK_KILL_REQUEST,
    /**
     * kill job request
     */
    TASK_SUBMIT_KILL_REQUEST,
    /**
     * kill job request
     */
    TASK_SUBMIT_KILL_ACK,
    /**
     * kill job request
     */
    TASK_KILL_ACK,
    /**
     * kill job response
     */
    TASK_KILL_RESPONSE,
    /**
     * job report info
     */
    TASK_REPORT_INFO,
    /**
     * ping
     */
    PING,
    /**
     * pong
     */
    PONG,
    /**
     *  roll view log request
     */
    ROLL_VIEW_LOG_REQUEST,

    /**
     *  roll view log response
     */
    ROLL_VIEW_LOG_RESPONSE,

    /**
     * view whole log request
     */
    VIEW_WHOLE_LOG_REQUEST,

    /**
     * view whole log response
     */
    VIEW_WHOLE_LOG_RESPONSE,

    /**
     * get log bytes request
     */
    GET_LOG_BYTES_REQUEST,

    /**
     * get log bytes response
     */
    GET_LOG_BYTES_RESPONSE,

    GET_JOB_BY_ID_REQUEST,

    GET_JOB_BY_ID_RESPONSE,

    UPDATE_JOB_REQUEST,

    UPDATE_JOB_RESPONSE,

    PERSIST_JOB_REQUEST,

    PERSIST_JOB_RESPONSE,

    GET_UN_STARTED_JOBS_REQUEST,

    GET_UN_STARTED_JOBS_RESPONSE,

    GET_UN_FINISHED_JOBS_REQUEST,

    GET_UN_FINISHED_JOBS_RESPONSE;
}
