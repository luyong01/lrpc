package com.ranze.literpc.codec;

import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        Protocol protocol = channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL).get();
        protocol.encodeResponse(byteBuf, rpcResponse);
    }
}
