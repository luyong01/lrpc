package com.ranze.literpc.codec;

import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class RpcRequestEncoder extends MessageToMessageEncoder<RpcRequest> {
    private LiteRpcClient liteRpcClient;

    public RpcRequestEncoder(LiteRpcClient client) {
        this.liteRpcClient = client;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest msg, List<Object> out) throws Exception {
        Protocol protocol = ctx.channel().attr(Consts.KEY_PROTOCOL).get();
        ByteBuf byteBuf = protocol.encodeRequest(msg);
        if (byteBuf != null) {
            out.add(byteBuf);
        }
    }
}
