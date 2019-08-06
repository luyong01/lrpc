package com.ranze.literpc.compress;

import com.google.protobuf.Message;
import com.ranze.literpc.codec.ProtoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class NoneCompress implements Compress {
    @Override
    public ByteBuf compress(Message message) {
        byte[] argBytes = ProtoSerializer.serialize(message);
        return Unpooled.wrappedBuffer(argBytes);
    }

    @Override
    public Message unCompress(ByteBuf byteBuf, Class clz) throws IllegalAccessException, IOException, InvocationTargetException {
        return ProtoSerializer.deserialize(clz, new ByteBufInputStream(byteBuf));
    }
}
