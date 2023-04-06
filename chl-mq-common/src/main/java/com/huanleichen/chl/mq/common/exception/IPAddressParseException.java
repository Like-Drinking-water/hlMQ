package com.huanleichen.chl.mq.common.exception;

public class IPAddressParseException extends RuntimeException {

    public IPAddressParseException() {
        super("IP 地址解析错误！");
    }


    public IPAddressParseException(String message) {
        super(message);
    }

    public IPAddressParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public IPAddressParseException(Throwable cause) {
        super(cause);
    }

    public IPAddressParseException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
