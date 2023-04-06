package com.huanleichen.chl.mq.bean;

import lombok.Data;

import java.net.InetSocketAddress;

@Data
public class Request extends BaseSendBean {

    // 表示请求的类型
    private Byte type;



    public Request() {

    }

    public Request(Message message) {
        super(message);
    }

    public Request(Message message, Byte type) {
        super(message);
        this.type = type;
    }

}
