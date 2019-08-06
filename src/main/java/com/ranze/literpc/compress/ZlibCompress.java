package com.ranze.literpc.compress;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;

public class ZlibCompress implements Compress {
    @Override
    public ByteBuf compress(Message message) {
        return null;
    }

    @Override
    public Message unCompress(ByteBuf byteBuf, Class clz) {
        return null;
    }
}
