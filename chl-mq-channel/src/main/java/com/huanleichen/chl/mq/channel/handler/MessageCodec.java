package com.huanleichen.chl.mq.channel.handler;

import com.huanleichen.chl.mq.bean.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
public class MessageCodec extends ByteToMessageCodec<BaseSendBean> {

    public MessageCodec() {}

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, BaseSendBean message, ByteBuf byteBuf) throws Exception {
        byte sendType = 3;
        // 1. 4 字节的魔数
        byteBuf.writeBytes(new byte[]{1, 2, 3, 4});
        // 2. 1 字节的版本,
        byteBuf.writeByte(1);
        // 3. 1 字节的序列化方式 jdk 0 , json 1
        byteBuf.writeByte(1);
        // 4. 1个字节的请求类型
        if (message instanceof Request) {
//            ResponseFuture responseFuture = new ResponseFuture();
//            waitResponse.put(message.getSendId(), responseFuture);
            sendType = ((Request) message).getType();
        }
        byteBuf.writeByte(sendType);
        // 5. 4 字节的请求ID
        byteBuf.writeInt(message.getSendId());

//        // 4. 1 字节的指令类型
//        byteBuf.writeByte(o.getMessageType());
//        // 5. 4 个字节
//        byteBuf.writeInt(o.getSequenceId());
//        // 无意义，对齐填充
//        byteBuf.writeByte(0xff);
        // 6. 获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message.getBody());
        log.debug("encode {}", message);
        byte[] bytes = bos.toByteArray();
        // 7. 长度
        byteBuf.writeInt(bytes.length);
        // 8. 写入内容
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magicNum = byteBuf.readInt();
        byte version = byteBuf.readByte();
        byte serializerType = byteBuf.readByte();
        byte messageType = byteBuf.readByte();
//        int sequenceId = byteBuf.readInt();
//        byteBuf.readByte();
        int sendId = byteBuf.readInt();
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes, 0, length);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object messageBody = ois.readObject();

        if (messageType == 1 || messageType == 0) {
            Request request = new Request();
            request.setType(messageType);
            request.setBody((Message) messageBody);

            request.setSendId(sendId);
            log.debug("{}, {}, {}, {}", magicNum, version, serializerType, messageType, length);
            log.debug("{}", messageBody);
            list.add(request);
        }
        else {
            Response response = new Response();
            response.setBody((Message) messageBody);
            response.setSendId(sendId);
            list.add(response);
//            ResponseFuture responseFuture = waitResponse.get(sendId);
//            responseFuture.setResponse(response);
//            waitResponse.remove(sendId);
        }


    }
}
