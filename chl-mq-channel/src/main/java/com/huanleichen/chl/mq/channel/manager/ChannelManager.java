package com.huanleichen.chl.mq.channel.manager;

import com.huanleichen.chl.mq.bean.Request;
import com.huanleichen.chl.mq.bean.Response;
import com.huanleichen.chl.mq.bean.ResponseFuture;
import com.huanleichen.chl.mq.channel.handler.MessageCodec;
import com.huanleichen.chl.mq.common.config.ClientStartConfig;
import com.huanleichen.chl.mq.common.exception.ChannelConnectException;
import com.huanleichen.chl.mq.common.exception.ChannelSendException;
import com.huanleichen.chl.mq.common.exception.SendTimeoutException;
import com.huanleichen.chl.mq.common.utils.IPAddressUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class ChannelManager {

    // 等待服务端回应的 Future
    private final Map<Integer, ResponseFuture> waitResponse = new ConcurrentHashMap<>();

    private static final long LOCK_TIMEOUT_MILLIS = 3000;



    protected ClientStartConfig clientStartConfig;

    protected final Map<String, ChannelWrapper> channelWrapperMap = new ConcurrentHashMap<>();

    private final ReentrantLock createChannelLock = new ReentrantLock();


    public ChannelManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        this.clientStartConfig = new ClientStartConfig();
        this.clientStartConfig.setConnectTimeoutMillis(100000);
    }

    protected abstract Bootstrap getClientBootstrap(ClientStartConfig config);

    public Response invokeSyn(String addr, final Request request, long timeout) throws InterruptedException {
        long beginTime = System.currentTimeMillis();
        final Channel channel = getChannel(addr);
        if (channel != null && channel.isActive()) {
            long costTime = System.currentTimeMillis() - beginTime;
            if (costTime >= timeout) {
                log.warn("发送超时了！ Address: [{}]， Request: [{}]", addr, request);
                throw new SendTimeoutException();
            }
            Response response = invokeSynImpl(channel, request, timeout - costTime);
            return response;
        }
        else {
            log.warn("Channel 连接无法建立");
            throw new ChannelConnectException();
        }
    }

    private Response invokeSynImpl(Channel channel, final Request request, long timeout) throws InterruptedException {
        ResponseFuture responseFuture = new ResponseFuture();
        waitResponse.put(request.getSendId(), responseFuture);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    responseFuture.setResponseStatus(true);
                }
                else {
                    responseFuture.setResponseStatus(false);
                    responseFuture.setResponse(null);
                }
            }
        });

        Response response = responseFuture.waitResponse(timeout);
        if (response == null) {
            if (responseFuture.isResponseStatus()) {
                log.warn("发送超时, Channel: [{}], Request: [{}]", channel, request);
                throw new SendTimeoutException();
            }
            else {
                throw new ChannelSendException();
            }
        }
        return response;
    }

    /**
     * 关闭 Channel
     * @param addr
     * @param channel
     */
    protected void closeChannel(final String addr, final Channel channel) {
        if (channel == null) {
            return;
        }
        final String channelAddr = ((null == addr) ? IPAddressUtils.parseAddress(channel) : addr);
        try {
            if (this.createChannelLock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean needToRemoveFromMap = true;
                    ChannelWrapper channelWrapper = this.channelWrapperMap.get(channelAddr);

                    if (channelWrapper == null) {
                        needToRemoveFromMap = false;
                    }
                    else if (channelWrapper.getChannel() != channel) {
                        needToRemoveFromMap = false;
                    }

                    if (needToRemoveFromMap) {
                        this.channelWrapperMap.remove(channelAddr);
                    }
                    channel.close();
                } catch (Exception e) {
                    log.error("关闭 Channel 失败", e);
                } finally {
                    this.createChannelLock.unlock();
                }


            }
        } catch (InterruptedException exception) {
            log.warn("关闭 Channel 失败");
        }

        channel.close();
        this.channelWrapperMap.remove(addr);
    }

    /**
     * 根据连接地址获取 Channel
     * @param addr
     * @return
     * @throws InterruptedException
     */
    private Channel getChannel(String addr) throws InterruptedException {
        if (addr == null) {
            return null;
        }

        ChannelWrapper channelWrapper = this.channelWrapperMap.get(addr);
        if (channelWrapper != null && channelWrapper.isOk()) {
            return channelWrapper.getChannel();
        }

        return this.createChannel(addr);
    }

    /**
     * 创建 Channel
     * @param addr
     * @return
     * @throws InterruptedException
     */
    private Channel createChannel(String addr) throws InterruptedException {
        ChannelWrapper channelWrapper = null;

        if (this.createChannelLock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean needCreateChannel = true;
                channelWrapper = this.channelWrapperMap.get(addr);
                if (channelWrapper != null) {
                    if (channelWrapper.isOk()) {
                        needCreateChannel = false;
                    }
                    else if (!channelWrapper.getChannelFuture().isDone()) {
                        needCreateChannel = false;
                    } else {
                        this.channelWrapperMap.remove(addr);
                        needCreateChannel = true;
                    }
                }
                else {
                    needCreateChannel = true;
                }
                if (needCreateChannel) {
                    Bootstrap clientBootstrap = getClientBootstrap(clientStartConfig);
                    String[] ipAndPort = IPAddressUtils.getIPAndPort(addr);
                    ChannelFuture connect = clientBootstrap.connect(ipAndPort[0], Integer.valueOf(ipAndPort[1]));
                    log.info("开始建立 Channel 连接 Address : [{}]", addr);
                    channelWrapper = new ChannelWrapper(connect);
                    this.channelWrapperMap.put(addr, channelWrapper);
                }
            } catch (Exception e) {
                log.error("创建 Channel 错误", e);
            } finally {
                this.createChannelLock.unlock();
            }

        }
        else {
            log.warn("创建 Channel 时尝试加锁超时 ： {} ms", LOCK_TIMEOUT_MILLIS);

        }

        if (channelWrapper != null) {
            if (channelWrapper.getChannelFuture().awaitUninterruptibly(this.clientStartConfig.getConnectTimeoutMillis())) {
                if (channelWrapper.isOk()) {
                    log.info("获取连接成功， Address: [{}]", addr);
                    return channelWrapper.getChannel();
                }
                else {
                    log.warn("获取连接失败， Address: [{}]", addr);
                }
            }
            else {
                log.warn("连接建立超时， Address: [{}]", addr);
            }
        }
        return null;
    }

    public abstract void shutdown();

    @Data
    @AllArgsConstructor
    protected class ChannelWrapper {
        @Setter(AccessLevel.NONE)
        private ChannelFuture channelFuture;

        public boolean isOk() {
            return this.channelFuture != null && this.channelFuture.channel().isActive();
        }

        public Channel getChannel() {
            return this.channelFuture.channel();
        }
    }

    protected class ClientHandler extends SimpleChannelInboundHandler<Response> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
            // 获取响应的 id，找到对应的 ResponseFuture
            ResponseFuture responseFuture = waitResponse.get(msg.getSendId());

            // 注入响应内容
            responseFuture.setResponse(msg);
        }
    }
}
