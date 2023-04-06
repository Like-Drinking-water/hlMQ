package com.huanleichen.chl.mq.common.exception;

public class ChannelConnectException extends RuntimeException {

    public ChannelConnectException() {
        super("Channel 连接错误：请检查 IP 地址及端口号！");
    }


    public ChannelConnectException(String message) {
        super(message);
    }

    public ChannelConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelConnectException(Throwable cause) {
        super(cause);
    }

    public ChannelConnectException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
