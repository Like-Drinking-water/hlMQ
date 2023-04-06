package com.huanleichen.chl.mq.client.consumer.impl;

import com.huanleichen.chl.mq.bean.Message;
import com.huanleichen.chl.mq.bean.Request;
import com.huanleichen.chl.mq.bean.Response;
import com.huanleichen.chl.mq.channel.manager.ChannelManager;
import com.huanleichen.chl.mq.channel.manager.TCPChannelManager;
import com.huanleichen.chl.mq.client.consumer.Consumer;
import com.huanleichen.chl.mq.common.constants.RequestConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SynConsumerImpl implements Consumer {

    private final static ChannelManager channelManager = new TCPChannelManager();


    @Override
    public Message pullMessageByTopic(String topicName) {
//        Promise<Message>[] promises = new Promise[1];
//        Channel channel = null;
//        try {
//            channel = new Bootstrap()
//                    .group(new NioEventLoopGroup(1))
//                    .handler(new ClientChannelInitializer(consumerHandler))
//                    .channel(NioSocketChannel.class).connect("localhost", 8666)
//                    .sync()
//                    .channel();
//        } catch (Exception e) {
//            log.error("{}", e);
//        }
//        log.info("remoteAddress {}", channel.remoteAddress().toString());
        Request request = new Request(null, RequestConstants.RequestTypeConstants.REQUEST_FOR_CONSUMER);

        Response response = null;


        try {
            response = channelManager.invokeSyn("localhost:8666", request, 50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (response == null) {
            return null;
        }

        return response.getBody();
    }

    public static void main(String[] args) {
        log.debug("成功获取数据 ： {}", new SynConsumerImpl().pullMessageByTopic(null));
    }
}
