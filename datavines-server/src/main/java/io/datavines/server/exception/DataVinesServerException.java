package io.datavines.server.exception;

import io.datavines.common.exception.DataVinesException;
import io.datavines.server.coordinator.api.enums.ApiStatus;
import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.Arrays;

public class DataVinesServerException extends DataVinesException {

    private ApiStatus status;

    public DataVinesServerException(String message) {
        super(message);
    }

    public DataVinesServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataVinesServerException(Throwable cause) {
        super(cause);
    }

    public DataVinesServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DataVinesServerException(ApiStatus status) {
        super(status.getMsg());
        this.status = status;
    }

    public DataVinesServerException(ApiStatus status, Throwable cause) {
        super(status.getMsg(), cause);
        this.status = status;
    }

    public DataVinesServerException(ApiStatus status, Object... statusParams) {
        super(CollectionUtils.isEmpty(Arrays.asList(statusParams)) ? status.getMsg() : MessageFormat.format(status.getMsg(), statusParams));
        this.status = status;
    }

    public ApiStatus getStatus() {
        return status;
    }
}
