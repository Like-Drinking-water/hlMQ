package com.huanleichen.chl.mq.channel.manager;

import com.google.gson.Gson;
import com.huanleichen.chl.mq.bean.BaseSendBean;
import com.huanleichen.chl.mq.bean.Message;
import com.huanleichen.chl.mq.bean.Request;
import com.huanleichen.chl.mq.bean.Response;
import com.huanleichen.chl.mq.channel.handler.MessageCodec;
import com.huanleichen.chl.mq.common.config.ClientStartConfig;
import com.huanleichen.chl.mq.common.constants.RequestConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
public class UDPChannelManager extends ChannelManager {

    private final EventLoopGroup eventLoopGroupWorker;

    public UDPChannelManager() {
        super();
        this.eventLoopGroupWorker = new NioEventLoopGroup(1);

    }

    protected Bootstrap getClientBootstrap(ClientStartConfig config) {
        Bootstrap bootstrap = new Bootstrap()
                .group(this.eventLoopGroupWorker)
                .channelFactory(new ChannelFactory<NioDatagramChannel>() {
                    @Override
                    public NioDatagramChannel newChannel() {
                        // 指定协议簇为 IPv4
                        return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                    }
                })
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new UDPOutputHandler(new InetSocketAddress("localhost", 8666)))
                                .addLast(new UDPInputHandler());
                    }
                });
        return bootstrap;
    }

    @Override
    public void shutdown() {
        try {
            this.eventLoopGroupWorker.shutdownGracefully();
            log.info("客户端关闭完成！");
        } catch (Exception e) {
            log.error("客户端关闭异常！", e);
        }
    }

    /**
     * UDP 消息输出处理
     */
    private class UDPOutputHandler extends ChannelOutboundHandlerAdapter {

        private InetSocketAddress address;

        public UDPOutputHandler(InetSocketAddress address) {
            this.address = address;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            ByteBuf buf = new UnpooledByteBufAllocator(true).buffer();
            buf.writeBytes(new Gson().toJson(msg).getBytes(StandardCharsets.UTF_8));
            DatagramPacket packet = new DatagramPacket(buf, address, new InetSocketAddress("localhost", 8888));
            ctx.writeAndFlush(packet);
        }

    }

    /**
     * UDP 消息输入处理
     */
    private class UDPInputHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            ByteBuf content = msg.content();

            Response response = new Gson().fromJson(content.toString(StandardCharsets.UTF_8), Response.class);
            ctx.fireChannelRead(response);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        UDPChannelManager udpChannelManager = new UDPChannelManager();
        Channel channel = udpChannelManager.getClientBootstrap(udpChannelManager.clientStartConfig).bind(8888).sync().channel();



        channel.writeAndFlush(new Request(null, RequestConstants.RequestTypeConstants.REQUEST_FOR_CONSUMER)).sync();



    }
}
