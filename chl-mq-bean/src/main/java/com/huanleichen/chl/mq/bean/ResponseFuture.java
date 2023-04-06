package com.huanleichen.chl.mq.bean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Data
public class ResponseFuture {
    // 计数器
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    // 响应消息
    @Setter(AccessLevel.NONE)
    private Response response;

    private boolean responseStatus = true;

    /**
     * 等待响应消息，进入阻塞状态
     * @param timeout 超时时间
     * @return
     * @throws InterruptedException
     */
    public Response waitResponse(long timeout) throws InterruptedException {
        countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        return this.response;
    }

    /**
     * 设置返回内容
     * @param response
     */
    public void setResponse(Response response) {
        this.response = response;
        countDownLatch.countDown();
    }

}
