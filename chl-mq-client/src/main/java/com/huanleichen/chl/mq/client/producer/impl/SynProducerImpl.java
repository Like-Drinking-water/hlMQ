package com.huanleichen.chl.mq.client.producer.impl;

import com.huanleichen.chl.mq.bean.Message;
import com.huanleichen.chl.mq.bean.Request;
import com.huanleichen.chl.mq.bean.Response;
import com.huanleichen.chl.mq.bean.Result;
import com.huanleichen.chl.mq.channel.manager.ChannelManager;
import com.huanleichen.chl.mq.channel.manager.TCPChannelManager;
import com.huanleichen.chl.mq.client.producer.Producer;
import com.huanleichen.chl.mq.channel.handler.MessageCodec;

import com.huanleichen.chl.mq.common.constants.RequestConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SynProducerImpl implements Producer {

    private static ChannelManager channelManager = new TCPChannelManager();

    @Override
    public Result sendMessageByTopic(Message message, String topicName) {
//        Channel channel = null;
//        try {
//            channel = new Bootstrap()
//                    .group(new NioEventLoopGroup(1))
//                    .handler(new ChannelInitializer<NioSocketChannel>() {
//                        @Override
//                        protected void initChannel(NioSocketChannel ch) throws Exception {
//                            ch.pipeline()
//                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
//                                    .addLast(new MessageCodec())
//                                    .addLast(new LoggingHandler(LogLevel.DEBUG));
//                        }
//                    })
//                    .channel(NioSocketChannel.class).connect("localhost", 8666)
//                    .sync()
//                    .channel();
//        } catch (Exception e) {
//            log.error("{}", e);
//        }
        Request request = new Request(message, RequestConstants.RequestTypeConstants.REQUEST_FOR_PRODUCER);
        try {
            channelManager.invokeSyn("localhost:8666", request, 50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        channel.writeAndFlush(request);
//        Response response = null;
//        try {
//            response = consumerHandler.invokeSend(channel, request, 500000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        if (response == null) {
//            return null;
//        }
//
//
//        return response.getBody();

        return null;
    }

    public static void main(String[] args) {
        Message message = new Message();
        message.setBody("chl");

        new SynProducerImpl().sendMessageByTopic(message, null);
    }
}
