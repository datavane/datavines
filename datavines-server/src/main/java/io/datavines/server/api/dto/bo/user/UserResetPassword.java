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
package io.datavines.server.api.dto.bo.user;

import io.datavines.common.CommonConstants;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@NotNull
public class UserResetPassword implements Serializable {

    @NotNull(message = "user id cannot be null")
    private Long id;

    @NotBlank(message = "old password must not be null")
    private String oldPassword;

    @NotBlank(message = "new password must not be null")
    @Pattern(regexp = CommonConstants.REG_USER_PASSWORD, message = "password length must between 6-20")
    private String newPassword;

    @NotBlank(message = "new password comfirm must not be null")
    @Pattern(regexp = CommonConstants.REG_USER_PASSWORD, message = "password length must between 6-20")
    private String newPasswordConfirm;
}
