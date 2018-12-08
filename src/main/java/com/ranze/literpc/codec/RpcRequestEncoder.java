package com.ranze.literpc.codec;

import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {
    private LiteRpcClient liteRpcClient;

    public RpcRequestEncoder(LiteRpcClient client) {
        this.liteRpcClient = client;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest msg, ByteBuf out) throws Exception {
        Protocol protocol = ctx.channel().attr(Consts.KEY_PROTOCOL).get();
        protocol.encodeRequest(out, msg);
    }
}
