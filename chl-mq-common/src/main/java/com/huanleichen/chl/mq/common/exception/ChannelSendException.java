package com.huanleichen.chl.mq.common.exception;

public class ChannelSendException extends RuntimeException {
    public ChannelSendException() {
        super("Channel 连接错误：请检查 IP 地址及端口号！");
    }


    public ChannelSendException(String message) {
        super(message);
    }

    public ChannelSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelSendException(Throwable cause) {
        super(cause);
    }

    public ChannelSendException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
