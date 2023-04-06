package com.huanleichen.chl.mq.client.consumer;

import com.huanleichen.chl.mq.bean.Message;
import com.huanleichen.chl.mq.bean.Result;

public interface Consumer {
    /**
     * 根据主题获取消息
     * @param topicName
     * @return
     */
    Message pullMessageByTopic(String topicName);
}
