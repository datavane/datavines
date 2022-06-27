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
package io.datavines.core.enums;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

public enum ApiStatus {

    /**
     * xx-xx-xxxx
     * 10 系统内置
     * - 01 系统
     * - 02 用户
     * 11 工作空间
     * 12 数据源
     * 13 任务
     * 14 SLAS 告警
     */
    SUCCESS(200, "success", "成功"),
    FAIL(400, "Bad Request", "错误的请求"),
    REQUEST_ERROR(10010001, "Request Error", "请求错误"),
    INVALID_TOKEN(10010002, "Invalid Token ：{0}", "无效的Token ：{0}"),
    TOKEN_IS_NULL_ERROR(10010002, "Token is Null Error", "Token为空错误"),
    USERNAME_HAS_BEEN_REGISTERED_ERROR(10020001, "The username {0} has been registered", "用户名 {0} 已被注册过"),
    REGISTER_USER_ERROR(10020002, "Register User {0} Error", "注册用户{0}失败"),
    USERNAME_OR_PASSWORD_ERROR(10020003, "Username or Email Error", "用户名或者密码错误"),
    USER_IS_NOT_EXIST_ERROR(10020004, "User is not exist", "用户不存在错误"),
    CREATE_VERIFICATION_IMAGE_ERROR(10020005, "create verification image error", "创建验证码错误"),
    EXPIRED_VERIFICATION_CODE(10020006, "expired verification code", "验证码已过期，请重新刷新"),
    INVALID_VERIFICATION_CODE(10020007, "invalid verification code", "错误的验证码，请重新输入"),

    WORKSPACE_EXIST_ERROR(11010001, "WorkSpace {0} is Exist error", "工作空间 {0} 已存在错误"),
    CREATE_WORKSPACE_ERROR(11010002, "Create WorkSpace {0} Error", "创建工作空间 {0} 错误"),
    WORKSPACE_NOT_EXIST_ERROR(11010003, "WorkSpace {0} is Not Exist Error", "工作空间 {0} 不存在错误"),
    UPDATE_WORKSPACE_ERROR(11010004, "Update WorkSpace {0} Error", "更新工作空间 {0} 错误"),

    DATASOURCE_EXIST_ERROR(12010001, "DataSource {0} is Exist error", "数据源 {0} 已存在错误"),
    CREATE_DATASOURCE_ERROR(12010002, "Create DataSource {0} Error", "创建数据源 {0} 错误"),
    DATASOURCE_NOT_EXIST_ERROR(12010003, "DataSource {0} is Not Exist Error", "数据源 {0} 不存在错误"),
    UPDATE_DATASOURCE_ERROR(12010004, "Update DataSource {0} Error", "更新数据源 {0} 错误"),
    GET_DATABASE_LIST_ERROR(12010005, "Get DataSource {0} Database List Error", "获取数据源 {0} 数据库列表错误"),
    GET_TABLE_LIST_ERROR(12010006, "Get DataSource {0} Database {1} Table List Error", "获取数据源 {0} 数据库 {1} 表列表错误"),
    GET_COLUMN_LIST_ERROR(12010007, "Get DataSource {0} Database {1} Table {2} Column List Error", "获取数据源 {0} 数据库 {1} 表 {2} 字段列表错误"),
    EXECUTE_SCRIPT_ERROR(12010008, "Execute Script {0} Error", "执行脚本 {0} 错误"),

    TASK_NOT_EXIST_ERROR(13010001, "Task {0} Not Exist Error", "任务{0}不存在错误"),
    TASK_LOG_PATH_NOT_EXIST_ERROR(13010002, "Task {0} Log Path  Not Exist Error", "任务{0}的日志路径不存在错误"),
    TASK_EXECUTE_HOST_NOT_EXIST_ERROR(13010003, "Task Execute Host {0} Not Exist Error", "任务{0}的执行服务地址不存在错误"),

    JOB_PARAMETER_IS_NULL_ERROR(14010001, "Job {0} Parameter is Null Error", "作业 {0} 参数为空错误"),
    CREATE_JOB_ERROR(14010002, "Create Job {0} Error", "创建作业 {0} 错误"),
    JOB_NOT_EXIST_ERROR(14010003, "Job {0} Not Exist Error", "作业 {0} 不存在错误"),
    JOB_EXIST_ERROR(14010004, "Job {0} is Exist error", "作业 {0} 已存在错误"),
    UPDATE_JOB_ERROR(14010005, "Update Job {0} Error", "更新作业 {0} 错误"),

    ADD_QUARTZ_ERROR(14020001, "Create Quartz {0} Error", "创建定时器{0}错误"),
    JOB_SCHEDULE_PARAMETER_IS_NULL_ERROR(14020002, "JobSchedule {0} Parameter is Null Error", "定时器 {0} 参数为空错误"),
    JOB_SCHEDULE_TYPE_NOT_VALIDATE_ERROR(14020003, "JobSchedule type {0} is not Validate Error", "定时器类型 {0} 错误"),
    JOB_SCHEDULE_EXIST_ERROR(14020004, "Job Schedule {0} is Exist error", "作业定时任务 {0} 已存在错误"),
    CREATE_JOB_SCHEDULE_ERROR(14020005, "Create Job Schedule {0} Error", "创建作业定时任务 {0} 错误"),
    JOB_SCHEDULE_NOT_EXIST_ERROR(14020006, "Job Schedule {0} is not Exist error", "作业定时任务 {0} 不存在错误"),
    UPDATE_JOB_SCHEDULE_ERROR(14020007, "Update Job Schedule {0} Error", "更新作业定时任务 {0} 错误"),

    CREATE_TENANT_ERROR(15010001, "Create Tenant {0} Error", "创建 Linux 用户 {0} 错误"),
    TENANT_NOT_EXIST_ERROR(15010002, "Tenant {0} Not Exist Error", "Linux 用户 {0} 不存在错误"),
    TENANT_EXIST_ERROR(15010003, "Tenant {0} is Exist error", "Linux 用户 {0} 已存在错误"),
    UPDATE_TENANT_ERROR(15010004, "Update Tenant {0} Error", "更新 Linux 用户 {0} 错误"),

    CREATE_ENV_ERROR(16010001, "Create Env {0} Error", "创建运行环境参数 {0} 错误"),
    ENV_NOT_EXIST_ERROR(16010002, "Env {0} Not Exist Error", "运行环境参数{0}不存在错误"),
    ENV_EXIST_ERROR(16010003, "Env {0} is Exist error", "运行环境参数 {0} 已存在错误"),
    UPDATE_ENV_ERROR(16010004, "Update Env {0} Error", "更新运行环境参数 {0} 错误"),
    SLAS_ALREADY_EXIST_ERROR(14010001, "SLAS {0} Already exist", "SLAS {0} 已经存在"),
    SLAS_SENDER_ALREADY_EXIST_ERROR(14020001, "SLAS Sender {0}  Already exist", "SLAS 发送器 {0} 已经存在"),
    ;

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ApiStatus(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }

    /**
     * Retrieve Status enum entity by status code.
     */
    public static Optional<ApiStatus> findStatusBy(int code) {
        for (ApiStatus status : ApiStatus.values()) {
            if (code == status.getCode()) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
