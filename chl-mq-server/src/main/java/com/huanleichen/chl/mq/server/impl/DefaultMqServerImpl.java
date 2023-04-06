package com.huanleichen.chl.mq.server.impl;

import com.google.gson.Gson;
import com.huanleichen.chl.mq.bean.*;
import com.huanleichen.chl.mq.channel.handler.MessageCodec;
import com.huanleichen.chl.mq.common.constants.RequestConstants;
import com.huanleichen.chl.mq.server.MqServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class DefaultMqServerImpl implements MqServer {
    // 当前插入的队列
    private int curQueue;

    // 队列数量
    private int numOfQueue;

    private final Map<String, Map<Integer, ConcurrentLinkedQueue<Message>>> topicQueueMap = new ConcurrentHashMap<>();

    // 队列隐射
    private final Map<Integer, ConcurrentLinkedQueue<Message>> queueMap = new HashMap<>();

    public void start() throws InterruptedException {
        log.info("################################# Start Beginning #################################");
        init(4);
        // TCP 版服务端
//        new ServerBootstrap()
//                .group(new NioEventLoopGroup(1), new NioEventLoopGroup(2))
//                .channel(NioServerSocketChannel.class)
//                .childHandler(new ChannelInitializer<NioSocketChannel>() {
//                    @Override
//                    protected void initChannel(NioSocketChannel ch) {
//                        ch.pipeline()
//                                .addLast(new LengthFieldBasedFrameDecoder(65535, 11, 4, 0, 0))
//                                .addLast(new MessageCodec())
//                                .addLast(new ChannelInboundHandlerAdapter() {
//                                    @Override
//                                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//                                        log.debug("msg {}", msg);
//
//                                        if (msg instanceof Request) {
//                                            Request message = (Request) msg;
//                                            if (message.getType().equals(RequestConstants.RequestTypeConstants.REQUEST_FOR_PRODUCER)) {
//                                                ConcurrentLinkedQueue<Message> queue = queueMap.get(curQueue);
//                                                queue.add(message.getBody());
//                                                log.debug("消息接收成功");
//                                                log.debug("{}", queue);
//                                                ctx.writeAndFlush("Success".getBytes(StandardCharsets.UTF_8));
//                                            }
//                                            else if (message.getType().equals(RequestConstants.RequestTypeConstants.REQUEST_FOR_CONSUMER)) {
//                                                ConcurrentLinkedQueue<Message> queue = queueMap.get(curQueue);
//                                                Response response = new Response();
//                                                Message mes = new Message();
//                                                response.setSendId(message.getSendId());
//                                                response.setBody(queue.poll());
//                                                ctx.writeAndFlush(response);
//                                                log.debug("消息传递成功");
//                                                log.debug("{}", queue);
//                                            }
//                                        }
//                                    }
//                                });
//                    }
//                }).bind(8666).sync();
        // UDP 版服务端
        new Bootstrap()
                .group(new NioEventLoopGroup(2))
                .channelFactory(new ChannelFactory<NioDatagramChannel>() {
                    @Override
                    public NioDatagramChannel newChannel() {
                        // 指定协议簇为 IPv4
                        return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                    }
                })
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ch.pipeline()
                                .addLast(new UDPOutputHandler())
                                .addLast(new UDPInputHandler());
                    }
                }).bind(8666).sync();
        log.info("################################# Start Over #################################");

    }

    /**
     * 初始化服务端
     * @param numOfQueue
     */
    private void init(int numOfQueue) {
        this.numOfQueue = numOfQueue;
        for (int i = 0; i < numOfQueue; i++) {
            this.queueMap.put(i, new ConcurrentLinkedQueue<>());
        }
    }

    private class UDPOutputHandler extends ChannelOutboundHandlerAdapter {


        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            ByteBuf buf = new UnpooledByteBufAllocator(true).buffer();
            buf.writeBytes(new Gson().toJson(msg).getBytes(StandardCharsets.UTF_8));
            DatagramPacket packet = new DatagramPacket(buf, ((Response) msg).getRecipient());
            ctx.writeAndFlush(packet);
        }

    }

    private class UDPInputHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            ByteBuf content = msg.content();
            Request message = new Gson().fromJson(content.toString(StandardCharsets.UTF_8), Request.class);
            log.debug("msg {}", msg);


            if (message.getType().equals(RequestConstants.RequestTypeConstants.REQUEST_FOR_PRODUCER)) {
                ConcurrentLinkedQueue<Message> queue = queueMap.get(curQueue);
                queue.add(message.getBody());
                log.debug("消息接收成功");
                log.debug("{}", queue);
                ctx.writeAndFlush("Success".getBytes(StandardCharsets.UTF_8));
            }
            else if (message.getType().equals(RequestConstants.RequestTypeConstants.REQUEST_FOR_CONSUMER)) {
                ConcurrentLinkedQueue<Message> queue = queueMap.get(curQueue);
                Response response = new Response();
                Message mes = new Message();
                response.setRecipient(msg.sender());
                response.setSendId(message.getSendId());
                response.setBody(queue.poll());
                ctx.writeAndFlush(response);
                log.debug("消息传递成功");
                log.debug("{}", queue);
            }
        }
    }

    /**
     * 接收客户端的消息
     * @param message
     * @return
     */
    private Result receiveMessage(Message message) {
        return null;
    }

    /**
     * 传递消息给客户端
     * @return
     */
    private Result deliverMessage() {
        return null;
    }

    public static void main(String[] args) {
        try {
            new DefaultMqServerImpl().start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
