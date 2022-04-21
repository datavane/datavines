package io.datavines.server.coordinator.api.enums;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

public enum ApiStatus {

    USERNAME_HAS_BEEN_REGISTERED_ERROR(10010001, "The username {0} has been registered", "用户名{0}已被注册过"),
    REGISTER_USER_ERROR(10010002, "Register User {0} Error", "注册用户{0}失败"),
    USERNAME_OR_PASSWORD_ERROR(10010003, "Username or Email Error", "用户名或者密码错误");

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
