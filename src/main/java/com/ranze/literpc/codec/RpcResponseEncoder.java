package com.ranze.literpc.codec;

import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

public class RpcResponseEncoder extends MessageToMessageEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, List<Object> out) throws Exception {
        Protocol protocol = channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL).get();
        ByteBuf byteBuf = protocol.encodeResponse(rpcResponse);
        if (byteBuf != null) {
            out.add(byteBuf);
        }
    }
}
