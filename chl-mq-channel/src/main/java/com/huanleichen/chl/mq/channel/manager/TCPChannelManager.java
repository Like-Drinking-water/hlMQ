package com.huanleichen.chl.mq.channel.manager;

import com.huanleichen.chl.mq.channel.handler.MessageCodec;
import com.huanleichen.chl.mq.common.config.ClientStartConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TCPChannelManager extends ChannelManager {

    private final EventLoopGroup eventLoopGroupWorker;

    public TCPChannelManager() {
        super();
        this.eventLoopGroupWorker = new NioEventLoopGroup(1);

    }

    protected Bootstrap getClientBootstrap(ClientStartConfig config) {
        Bootstrap bootstrap = new Bootstrap()
                .group(this.eventLoopGroupWorker)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new LengthFieldBasedFrameDecoder(65535, 11, 4, 0, 0))
                                .addLast(new MessageCodec())
                                .addLast(new ClientHandler());
                    }
                });
        return bootstrap;
    }

    public void shutdown() {
        try {
            for (String addr : this.channelWrapperMap.keySet()) {
                closeChannel(addr, this.channelWrapperMap.get(addr).getChannel());
            }
            this.channelWrapperMap.clear();

            this.eventLoopGroupWorker.shutdownGracefully();
            log.info("客户端关闭完成！");
        } catch (Exception e) {
            log.error("客户端关闭异常！", e);
        }
    }
}
