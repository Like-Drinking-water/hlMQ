package com.huanleichen.chl.mq.bean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class BaseSendBean implements Serializable {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static AtomicInteger currentId = new AtomicInteger(0);

    // 请求或响应的 ID
    private Integer sendId = currentId.getAndIncrement();

    // 发送的消息
    private Message body;

    // 接收地址
    private InetSocketAddress recipient;



    public BaseSendBean() {

    }

    public BaseSendBean(Message message) {
        this.body = message;
    }

}
