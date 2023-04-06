package com.huanleichen.chl.mq.client.producer;

import com.huanleichen.chl.mq.bean.Message;
import com.huanleichen.chl.mq.bean.Result;

public interface Producer {
    /**
     * 根据主题发布消息
     * @param message
     * @param topicName
     * @return
     */
    Result sendMessageByTopic(Message message, String topicName);
}
