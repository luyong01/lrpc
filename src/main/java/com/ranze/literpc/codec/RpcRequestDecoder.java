package com.ranze.literpc.codec;

import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcRequestDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Protocol protocol = channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL).get();
        list.add(protocol.decodeResponse(byteBuf));
    }
}
