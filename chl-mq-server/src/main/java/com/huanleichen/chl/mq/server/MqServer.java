package com.huanleichen.chl.mq.server;

import com.huanleichen.chl.mq.bean.Message;
import com.huanleichen.chl.mq.bean.Result;

public interface MqServer {
    /**
     * 启动服务端
     */
    void start() throws InterruptedException;

}
