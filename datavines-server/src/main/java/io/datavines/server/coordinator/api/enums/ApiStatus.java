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
package io.datavines.server.coordinator.api.enums;

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
     *
     */
    SUCCESS(200, "success", "成功"),
    FAIL(400, "Bad Request", "错误的请求"),
    REQUEST_ERROR(10010001, "Request Error", "请求错误"),
    INVALID_TOKEN(10010002, "Invalid Token ：{0}", "无效的Token ：{0}"),
    TOKEN_IS_NULL_ERROR(10010002, "Token is Null Error", "Token为空错误"),
    USERNAME_HAS_BEEN_REGISTERED_ERROR(10020001, "The username {0} has been registered", "用户名{0}已被注册过"),
    REGISTER_USER_ERROR(10020002, "Register User {0} Error", "注册用户{0}失败"),
    USERNAME_OR_PASSWORD_ERROR(10020003, "Username or Email Error", "用户名或者密码错误"),
    USER_IS_NOT_EXIST_ERROR(10020004, "User is not exist", "用户不存在错误"),
    CREAT_VERIFICATION_IMAGE_ERROR(10020005, "creat verification image error", "创建验证码错误"),
    EXPIRED_VERIFICATION_CODE(10020006, "expired verification code", "验证码已过期，请重新刷新"),
    INVALID_VERIFICATION_CODE(10020007, "invalid verification code", "错误的验证码，请重新输入"),

    WORKSPACE_EXIST_ERROR(11010001, "WorkSpace {0} is Exist error", "工作空间{0}已存在错误"),
    CREATE_WORKSPACE_ERROR(11010002, "Create WorkSpace {0} Error", "创建工作空间{0}错误"),
    WORKSPACE_NOT_EXIST_ERROR(11010003, "WorkSpace {0} is Not Exist Error", "工作空间{0}不存在错误"),
    UPDATE_WORKSPACE_ERROR(11010004, "Update WorkSpace {0} Error", "更新工作空间{0}错误"),

    DATASOURCE_EXIST_ERROR(12010001, "DataSource {0} is Exist error", "数据源{0}已存在错误"),
    CREATE_DATASOURCE_ERROR(12010002, "Create DataSource {0} Error", "创建数据源{0}错误"),
    DATASOURCE_NOT_EXIST_ERROR(12010003, "DataSource {0} is Not Exist Error", "数据源{0}不存在错误"),
    UPDATE_DATASOURCE_ERROR(12010004, "Update DataSource {0} Error", "更新数据源{0}错误"),
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
