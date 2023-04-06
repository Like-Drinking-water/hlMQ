package com.huanleichen.chl.mq.common.exception;

public class SendTimeoutException extends RuntimeException {
    public SendTimeoutException() {
        super("发送超时！");
    }


    public SendTimeoutException(String message) {
        super(message);
    }

    public SendTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendTimeoutException(Throwable cause) {
        super(cause);
    }

    public SendTimeoutException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
